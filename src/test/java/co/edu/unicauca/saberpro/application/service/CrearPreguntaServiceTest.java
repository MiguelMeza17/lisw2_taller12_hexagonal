package co.edu.unicauca.saberpro.application.service;

import co.edu.unicauca.saberpro.domain.exception.PreguntaNoEncontradaException;
import co.edu.unicauca.saberpro.domain.exception.ReglaNegocioException;
import co.edu.unicauca.saberpro.domain.model.EstadoPregunta;
import co.edu.unicauca.saberpro.domain.model.Pregunta;
import co.edu.unicauca.saberpro.domain.ports.inbound.CrearPreguntaComando;
import co.edu.unicauca.saberpro.domain.ports.inbound.CrearPreguntaComando.Opcion;
import co.edu.unicauca.saberpro.domain.ports.outbound.NotificacionPort;
import co.edu.unicauca.saberpro.domain.ports.outbound.PreguntaRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del servicio de aplicación con dobles de los puertos de salida (sin Spring).
 */
class CrearPreguntaServiceTest {

    private final Map<Long, Pregunta> almacen = new HashMap<>();
    private final List<Pregunta> notificadas = new ArrayList<>();
    private CrearPreguntaService servicio;

    @BeforeEach
    void preparar() {
        PreguntaRepositoryPort repositorio = new PreguntaRepositoryPort() {
            private long secuencia = 0;

            @Override
            public Pregunta save(Pregunta pregunta) {
                if (pregunta.getId() == null) {
                    pregunta.asignarId(++secuencia);
                }
                almacen.put(pregunta.getId(), pregunta);
                return pregunta;
            }

            @Override
            public Optional<Pregunta> findById(Long id) {
                return Optional.ofNullable(almacen.get(id));
            }
        };
        NotificacionPort notificacion = notificadas::add;
        servicio = new CrearPreguntaService(repositorio, notificacion);
    }

    private static CrearPreguntaComando comando(List<Opcion> opciones) {
        return new CrearPreguntaComando("¿Qué patrón separa el dominio de la infraestructura?",
                "Los puertos y adaptadores aíslan el núcleo de la tecnología.", opciones);
    }

    private static List<Opcion> cuatroOpcionesUnaClave() {
        return List.of(new Opcion("Hexagonal", true), new Opcion("Singleton", false),
                new Opcion("Monolito", false), new Opcion("Capas acopladas", false));
    }

    @Test
    void crearGuardaLaPreguntaEnBorrador() {
        Pregunta creada = servicio.crear(comando(cuatroOpcionesUnaClave()));

        assertNotNull(creada.getId());
        assertEquals(EstadoPregunta.BORRADOR, creada.getEstado());
        assertEquals(4, creada.getOpciones().size());
        assertTrue(almacen.containsKey(creada.getId()));
    }

    @Test
    void enviarARevisionCambiaEstadoYNotifica() {
        Long id = servicio.crear(comando(cuatroOpcionesUnaClave())).getId();

        Pregunta enviada = servicio.enviarARevision(id);

        assertEquals(EstadoPregunta.PENDIENTE_REVISION, enviada.getEstado());
        assertEquals(List.of(enviada), notificadas);
    }

    @Test
    void enviarARevisionSinCuatroOpcionesNoNotifica() {
        Long id = servicio.crear(comando(List.of(new Opcion("Hexagonal", true), new Opcion("Singleton", false)))).getId();

        assertThrows(ReglaNegocioException.class, () -> servicio.enviarARevision(id));
        assertTrue(notificadas.isEmpty());
        assertEquals(EstadoPregunta.BORRADOR, servicio.obtener(id).getEstado());
    }

    @Test
    void preguntaInexistenteLanzaNoEncontrada() {
        assertThrows(PreguntaNoEncontradaException.class, () -> servicio.enviarARevision(99L));
        assertThrows(PreguntaNoEncontradaException.class, () -> servicio.obtener(99L));
    }
}
