# lisw2_taller12_hexagonal

Taller 12 - Arquitectura Hexagonal y DDD (Fase I) del Banco de Preguntas Saber Pro.
Laboratorio de Ingeniería de Software II, Universidad del Cauca, 2026.2.

## Tecnologías

Java 17, Spring Boot 3.3, Spring Data JPA, H2 en memoria, Maven y JUnit 5.

## Estructura de paquetes

El dominio parte del modelo del taller 11 ([`lisw2_taller11_ddd`](https://github.com/MiguelMeza17/lisw2_taller11_ddd)).
Las dependencias apuntan siempre hacia `domain`, que es Java puro (sin Spring ni JPA).

```
co.edu.unicauca.saberpro
├── SaberProApplication.java          arranque de Spring Boot
├── domain/                           núcleo del hexágono (Java puro)
│   ├── model/                        Pregunta (raíz de agregado), Enunciado, Justificacion, OpcionRespuesta, EstadoPregunta
│   ├── exception/                    DomainException, ReglaNegocioException
│   └── ports/
│       ├── inbound/                  puertos de entrada (casos de uso)
│       └── outbound/                 puertos de salida (persistencia, notificaciones)
├── application/
│   └── service/                      implementación de los casos de uso
└── infrastructure/
    ├── adapter/in/web/               controlador REST
    ├── adapter/out/persistence/jpa/  JpaQuestionAdapter (H2)
    ├── adapter/out/persistence/memory/ InMemoryQuestionAdapter (HashMap)
    └── config/                       conexión de adaptadores con puertos
```

## Diagramas

Fuentes en `docs/diagramas/*.mmd` (Mermaid; GitHub los dibuja directamente).

### Puertos y adaptadores

```mermaid
graph TD
    Cliente(["Cliente HTTP<br/>(Thunder Client / Postman)"])

    subgraph DrivingAdapters["Adaptadores de entrada (driving)"]
        Controller["PreguntaController<br/>(infrastructure/adapter/in/web)"]
    end

    subgraph Hexagono["Núcleo del hexágono"]
        subgraph PortsIn["Puertos de entrada (domain.ports.inbound)"]
            CrearUC["CrearPreguntaUseCase"]
            ConsultarUC["ConsultarPreguntaUseCase"]
        end
        subgraph App["Capa de aplicación"]
            Service["CrearPreguntaService<br/>(application/service)"]
        end
        subgraph Dominio["Dominio puro (domain.model)"]
            Pregunta["Pregunta<br/>(raíz de agregado)"]
            VOs["Enunciado · Justificacion · OpcionRespuesta<br/>(objetos de valor)"]
            Estado["EstadoPregunta"]
        end
        subgraph PortsOut["Puertos de salida (domain.ports.outbound)"]
            RepoPort["PreguntaRepositoryPort<br/>save · findById"]
            NotifPort["NotificacionPort"]
        end
    end

    subgraph DrivenAdapters["Adaptadores de salida (driven)"]
        JpaAdapter["JpaQuestionAdapter<br/>(persistence/jpa)"]
        MemAdapter["InMemoryQuestionAdapter<br/>(persistence/memory)"]
        LogAdapter["Adaptador de notificaciones<br/>(log)"]
        SpringData["SpringDataPreguntaRepository"]
        Mapa[("HashMap")]
        H2[("H2<br/>jdbc:h2:mem:saberprodb")]
    end

    Config{{"HexagonalConfiguration<br/>saberpro.persistencia = jpa | memoria"}}

    Cliente -->|"HTTP"| Controller
    Controller -->|"usa"| CrearUC
    Controller -->|"usa"| ConsultarUC
    Service -.->|"implementa"| CrearUC
    Service -.->|"implementa"| ConsultarUC
    Service -->|"opera sobre"| Pregunta
    Pregunta --> VOs
    Pregunta --> Estado
    Service -->|"guarda / carga"| RepoPort
    Service -->|"notifica"| NotifPort
    JpaAdapter -.->|"implementa"| RepoPort
    MemAdapter -.->|"implementa"| RepoPort
    LogAdapter -.->|"implementa"| NotifPort
    JpaAdapter --> SpringData --> H2
    MemAdapter --> Mapa
    Config -.->|"elige con @Bean"| JpaAdapter
    Config -.->|"elige con @Bean"| MemAdapter
```

### Secuencia: crear pregunta y enviarla a revisión

```mermaid
sequenceDiagram
    autonumber
    actor Cliente as Cliente HTTP
    participant Controller as PreguntaController<br/>(adaptador entrada)
    participant UC as CrearPreguntaUseCase<br/>(puerto entrada)
    participant Service as CrearPreguntaService<br/>(aplicación)
    participant Pregunta as Pregunta<br/>(raíz de agregado)
    participant RepoPort as PreguntaRepositoryPort<br/>(puerto salida)
    participant Adapter as JpaQuestionAdapter /<br/>InMemoryQuestionAdapter
    participant Notif as NotificacionPort<br/>(puerto salida)

    Note over Cliente,Notif: Crear pregunta
    Cliente->>Controller: POST /api/preguntas
    Controller->>UC: crear(CrearPreguntaComando)
    UC->>Service: crear(comando)
    Service->>Pregunta: new Pregunta(Enunciado, Justificacion)
    Pregunta-->>Service: valida VOs (justificación ≥ 15)
    Service->>Pregunta: agregarOpcion(texto, esCorrecta) ×4
    Service->>RepoPort: save(pregunta)
    RepoPort->>Adapter: save(pregunta)
    Adapter-->>Service: pregunta con id (BORRADOR)
    Service-->>Controller: Pregunta
    Controller-->>Cliente: 201 Created

    Note over Cliente,Notif: Enviar a revisión
    Cliente->>Controller: POST /api/preguntas/{id}/enviar-revision
    Controller->>UC: enviarARevision(id)
    UC->>Service: enviarARevision(id)
    Service->>RepoPort: findById(id)
    RepoPort->>Adapter: findById(id)
    Adapter-->>Service: Optional con la pregunta
    Service->>Pregunta: enviarARevision()
    alt 4 opciones y 1 sola clave
        Pregunta-->>Service: estado = PENDIENTE_REVISION
        Service->>RepoPort: save(pregunta)
        Service->>Notif: notificarEnvioARevision(pregunta)
        Service-->>Controller: Pregunta
        Controller-->>Cliente: 200 OK
    else invariante incumplida
        Pregunta-->>Service: ReglaNegocioException
        Service-->>Controller: ReglaNegocioException
        Controller-->>Cliente: 400 / 422
    end
```

### Clases del dominio y puertos

```mermaid
classDiagram
    direction LR

    class Pregunta {
        <<AggregateRoot>>
        -Long id
        -EstadoPregunta estado
        +NUMERO_OPCIONES = 4
        +agregarOpcion(String, boolean)
        +enviarARevision()
        +iniciarRevision()
        +aprobar()
        +rechazar()
        +publicar()
        +archivar()
        +getOpciones() List~OpcionRespuesta~
        -validarInvariantesParaEnvio()
    }
    class Enunciado {
        <<ValueObject>>
        -String texto
    }
    class Justificacion {
        <<ValueObject>>
        -String texto
        +LONGITUD_MINIMA = 15
    }
    class OpcionRespuesta {
        <<ValueObject>>
        -String texto
        -boolean esCorrecta
    }
    class EstadoPregunta {
        <<enumeration>>
        BORRADOR
        PENDIENTE_REVISION
        EN_REVISION
        APROBADA
        PUBLICADA
        ARCHIVADA
        +puedeTransitarA(EstadoPregunta) boolean
    }
    class DomainException
    class ReglaNegocioException
    class PreguntaNoEncontradaException

    class CrearPreguntaUseCase {
        <<interface>>
        +crear(CrearPreguntaComando) Pregunta
        +enviarARevision(Long) Pregunta
    }
    class ConsultarPreguntaUseCase {
        <<interface>>
        +obtener(Long) Pregunta
    }
    class CrearPreguntaComando {
        <<record>>
        +String enunciado
        +String justificacion
        +List~Opcion~ opciones
    }
    class PreguntaRepositoryPort {
        <<interface>>
        +save(Pregunta) Pregunta
        +findById(Long) Optional~Pregunta~
    }
    class NotificacionPort {
        <<interface>>
        +notificarEnvioARevision(Pregunta)
    }
    class CrearPreguntaService {
        -PreguntaRepositoryPort repositorio
        -NotificacionPort notificaciones
    }

    Pregunta *-- "1" Enunciado
    Pregunta *-- "1" Justificacion
    Pregunta *-- "0..4" OpcionRespuesta
    Pregunta --> EstadoPregunta
    DomainException <|-- ReglaNegocioException
    DomainException <|-- PreguntaNoEncontradaException
    Pregunta ..> ReglaNegocioException : lanza
    CrearPreguntaUseCase ..> CrearPreguntaComando
    CrearPreguntaUseCase <|.. CrearPreguntaService
    ConsultarPreguntaUseCase <|.. CrearPreguntaService
    CrearPreguntaService --> PreguntaRepositoryPort
    CrearPreguntaService --> NotificacionPort
    CrearPreguntaService ..> Pregunta
```

## Informe técnico y evidencias

- Informe en la plantilla oficial: [`docs/lisw2_taller12_g1.docx`](docs/lisw2_taller12_g1.docx) (modelo hexagonal, modelo DDD, diagramas, evidencias de intercambiabilidad y resultado de las pruebas).
- Diagramas en PNG: [`docs/diagramas/`](docs/diagramas/) (generados desde los `.mmd`).
- Evidencias del experimento de intercambiabilidad: [`docs/evidencias/intercambiabilidad.md`](docs/evidencias/intercambiabilidad.md).

## Ejecutar

```bash
mvn test              # pruebas
mvn spring-boot:run   # API en http://localhost:8080, consola H2 en /h2-console (jdbc:h2:mem:saberprodb, usuario sa)
```

## Pruebas

`mvn test` ejecuta pruebas JUnit 5 en tres niveles:

| Nivel | Clases | Qué verifican |
|---|---|---|
| Dominio (Java puro, sin Spring) | `EnunciadoTest`, `JustificacionTest`, `OpcionRespuestaTest`, `PreguntaTest` | Value Objects inmutables (nulo/vacío, mínimo 15 caracteres, igualdad por valor); agregado `Pregunta` con 3 o 5 opciones, 0 o 2 claves, transición BORRADOR → PENDIENTE_REVISION y reconstitución |
| Aplicación | `CrearPreguntaServiceTest` | `CrearPreguntaService` con el adaptador real `InMemoryQuestionAdapter` y un `NotificacionPort` falso (`NotificacionPortFalso`) |
| Arquitectura | `DominioPuroTest` | `domain` y `application` no importan Spring, JPA ni `infrastructure` (misma regla que el workflow de CI) |
| Adaptadores | `JpaQuestionAdapterTest`, `InMemoryQuestionAdapterTest`, `HexagonalConfigurationTest` | Cada adaptador cumple el contrato del puerto y la configuración elige el adaptador según `saberpro.persistencia` |
| Integración (`@SpringBootTest` + MockMvc) | `PreguntaControllerTest`, `IntercambiabilidadTest` | El controlador REST con los **mismos asertos** ejecutados con el adaptador JPA y con el de memoria |

## Intercambiar el adaptador de persistencia

`infrastructure/config/HexagonalConfiguration` declara con `@Bean` qué adaptador implementa
`PreguntaRepositoryPort`, según la propiedad `saberpro.persistencia`:

| Valor | Adaptador | Almacenamiento |
|---|---|---|
| `jpa` (por defecto) | `JpaQuestionAdapter` | H2 en memoria vía Spring Data JPA |
| `memoria` | `InMemoryQuestionAdapter` | `HashMap` |

Se cambia en `application.properties` o al arrancar, sin tocar el paquete `domain`:

```bash
mvn spring-boot:run                                                   # JPA
mvn spring-boot:run -Dspring-boot.run.arguments=--saberpro.persistencia=memoria   # memoria
```
