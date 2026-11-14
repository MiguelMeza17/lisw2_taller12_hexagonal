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

## Ejecutar

```bash
mvn test              # pruebas
mvn spring-boot:run   # API en http://localhost:8080, consola H2 en /h2-console (jdbc:h2:mem:saberprodb, usuario sa)
```

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
