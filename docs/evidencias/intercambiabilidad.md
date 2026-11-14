# Prueba de intercambiabilidad (sección 4 del taller)

El objetivo es demostrar que `PreguntaRepositoryPort` puede implementarse con `JpaQuestionAdapter` (H2)
o con `InMemoryQuestionAdapter` (HashMap) y que la aplicación se comporta exactamente igual **sin tocar
una sola línea del paquete `domain`**.

El adaptador se elige en `infrastructure/config/HexagonalConfiguration` con dos `@Bean` condicionados a la
propiedad `saberpro.persistencia`:

```java
@Bean
@ConditionalOnProperty(name = PROPIEDAD, havingValue = "jpa", matchIfMissing = true)
public PreguntaRepositoryPort jpaQuestionAdapter(SpringDataPreguntaRepository repositorio) { ... }

@Bean
@ConditionalOnProperty(name = PROPIEDAD, havingValue = "memoria")
public PreguntaRepositoryPort inMemoryQuestionAdapter() { ... }
```

## Paso 1: aplicación con `JpaQuestionAdapter`

Peticiones REST reales contra el `.jar` (`mvn package`) arrancado con el adaptador JPA:

```text
$ java -jar target/saberpro-hexagonal-0.1.0.jar --saberpro.persistencia=jpa
c.e.u.s.i.config.HexagonalConfiguration  : Adaptador de persistencia activo: JpaQuestionAdapter (H2)
c.e.u.saberpro.SaberProApplication       : Started SaberProApplication in 23.158 seconds (process running for 23.775)

$ curl -X POST http://localhost:18080/api/preguntas
{"id":1,"enunciado":"¿Qué patrón aísla el dominio de la infraestructura?","justificacion":"Los puertos y adaptadores aíslan el núcleo.","estado":"BORRADOR","opciones":[{"texto":"Hexagonal","esCorrecta":true},{"texto":"Singleton","esCorrecta":false},{"texto":"Monolito","esCorrecta":false},{"texto":"Capas acopladas","esCorrecta":false}]}
HTTP 201

$ curl -X POST http://localhost:18080/api/preguntas/1/enviar-revision
{"id":1,"enunciado":"¿Qué patrón aísla el dominio de la infraestructura?","justificacion":"Los puertos y adaptadores aíslan el núcleo.","estado":"PENDIENTE_REVISION","opciones":[{"texto":"Hexagonal","esCorrecta":true},{"texto":"Singleton","esCorrecta":false},{"texto":"Monolito","esCorrecta":false},{"texto":"Capas acopladas","esCorrecta":false}]}
HTTP 200

$ curl -X GET http://localhost:18080/api/preguntas/1
{"id":1,"enunciado":"¿Qué patrón aísla el dominio de la infraestructura?","justificacion":"Los puertos y adaptadores aíslan el núcleo.","estado":"PENDIENTE_REVISION","opciones":[{"texto":"Hexagonal","esCorrecta":true},{"texto":"Singleton","esCorrecta":false},{"texto":"Monolito","esCorrecta":false},{"texto":"Capas acopladas","esCorrecta":false}]}
HTTP 200

$ curl -X POST http://localhost:18080/api/preguntas
{"estado":400,"error":"Regla de negocio","mensaje":"La justificación debe contener al menos 15 caracteres.","fecha":"2026-11-14T02:56:31.689931300Z"}
HTTP 400

$ curl -X GET http://localhost:18080/api/preguntas/9999
{"estado":404,"error":"No encontrada","mensaje":"No existe la pregunta con id 9999.","fecha":"2026-11-14T02:56:31.804868500Z"}
HTTP 404

La pregunta 1 quedó en estado PENDIENTE_REVISION y espera asignación de revisores.
```

## Paso 2: cambio de adaptador a `InMemoryQuestionAdapter`

El cambio es solo de configuración: `saberpro.persistencia=memoria` en `application.properties`, o como
argumento al arrancar. No se modifica ninguna clase.

Las mismas peticiones, ahora con el adaptador en memoria:

```text
$ java -jar target/saberpro-hexagonal-0.1.0.jar --saberpro.persistencia=memoria
c.e.u.s.i.config.HexagonalConfiguration  : Adaptador de persistencia activo: InMemoryQuestionAdapter (HashMap)
c.e.u.saberpro.SaberProApplication       : Started SaberProApplication in 19.953 seconds (process running for 20.897)

$ curl -X POST http://localhost:18080/api/preguntas
{"id":1,"enunciado":"¿Qué patrón aísla el dominio de la infraestructura?","justificacion":"Los puertos y adaptadores aíslan el núcleo.","estado":"BORRADOR","opciones":[{"texto":"Hexagonal","esCorrecta":true},{"texto":"Singleton","esCorrecta":false},{"texto":"Monolito","esCorrecta":false},{"texto":"Capas acopladas","esCorrecta":false}]}
HTTP 201

$ curl -X POST http://localhost:18080/api/preguntas/1/enviar-revision
{"id":1,"enunciado":"¿Qué patrón aísla el dominio de la infraestructura?","justificacion":"Los puertos y adaptadores aíslan el núcleo.","estado":"PENDIENTE_REVISION","opciones":[{"texto":"Hexagonal","esCorrecta":true},{"texto":"Singleton","esCorrecta":false},{"texto":"Monolito","esCorrecta":false},{"texto":"Capas acopladas","esCorrecta":false}]}
HTTP 200

$ curl -X GET http://localhost:18080/api/preguntas/1
{"id":1,"enunciado":"¿Qué patrón aísla el dominio de la infraestructura?","justificacion":"Los puertos y adaptadores aíslan el núcleo.","estado":"PENDIENTE_REVISION","opciones":[{"texto":"Hexagonal","esCorrecta":true},{"texto":"Singleton","esCorrecta":false},{"texto":"Monolito","esCorrecta":false},{"texto":"Capas acopladas","esCorrecta":false}]}
HTTP 200

$ curl -X POST http://localhost:18080/api/preguntas
{"estado":400,"error":"Regla de negocio","mensaje":"La justificación debe contener al menos 15 caracteres.","fecha":"2026-11-14T02:56:55.541292900Z"}
HTTP 400

$ curl -X GET http://localhost:18080/api/preguntas/9999
{"estado":404,"error":"No encontrada","mensaje":"No existe la pregunta con id 9999.","fecha":"2026-11-14T02:56:55.643020900Z"}
HTTP 404

La pregunta 1 quedó en estado PENDIENTE_REVISION y espera asignación de revisores.
```

Las respuestas son idénticas en ambos modos: 201, 200 (PENDIENTE_REVISION), 200, 400 y 404.

## Paso 3: pruebas unitarias e integración con ambos adaptadores

`IntercambiabilidadTest` ejecuta el **mismo flujo con los mismos asertos** (crear → enviar a revisión →
consultar → regla incumplida) en dos contextos de Spring, uno por adaptador, y verifica qué clase quedó
conectada al puerto:

```text
$ mvn test
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.072 s -- in co.edu.unicauca.saberpro.application.service.CrearPreguntaServiceTest
Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 5.695 s -- in co.edu.unicauca.saberpro.infrastructure.adapter.in.web.PreguntaControllerTest
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.930 s -- in co.edu.unicauca.saberpro.infrastructure.adapter.out.persistence.jpa.JpaQuestionAdapterTest
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.018 s -- in co.edu.unicauca.saberpro.infrastructure.adapter.out.persistence.memory.InMemoryQuestionAdapterTest
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.768 s -- in co.edu.unicauca.saberpro.infrastructure.config.HexagonalConfigurationTest
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.695 s -- in co.edu.unicauca.saberpro.intercambiabilidad.IntercambiabilidadTest$ConAdaptadorEnMemoria
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.513 s -- in co.edu.unicauca.saberpro.intercambiabilidad.IntercambiabilidadTest$ConAdaptadorJpa
Tests run: 0, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.221 s -- in co.edu.unicauca.saberpro.intercambiabilidad.IntercambiabilidadTest
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.029 s -- in co.edu.unicauca.saberpro.SaberProApplicationTests
Tests run: 23, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## El dominio no cambió

El paquete `domain` no importa Spring ni JPA:

```text
$ grep -rnE "org.springframework|jakarta.persistence" src/main/java/co/edu/unicauca/saberpro/domain
(sin resultados)
```

Últimos cambios en `domain`: ninguno desde que se definieron los puertos (los adaptadores, el controlador
y la configuración se agregaron después sin tocarlo):

```text
$ git log --oneline -- src/main/java/co/edu/unicauca/saberpro/domain
e794d01 Definir puertos de entrada y salida del hexagono
3beb293 Configurar proyecto Spring Boot con estructura hexagonal
```

## Capturas pendientes para el informe

- Consola H2 (`http://localhost:8080/h2-console`, `jdbc:h2:mem:saberprodb`, usuario `sa`) con las tablas
  `preguntas` y `opciones_respuesta` después del paso 1.
- Thunder Client ejecutando `docs/thunder-client/thunder-collection_saberpro-taller12.json` en cada modo.
