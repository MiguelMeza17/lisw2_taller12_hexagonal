package co.edu.unicauca.saberpro.application.service;

import co.edu.unicauca.saberpro.domain.exception.PreguntaNoEncontradaException;
import co.edu.unicauca.saberpro.domain.exception.ReglaNegocioException;
import co.edu.unicauca.saberpro.domain.model.EstadoPregunta;
import co.edu.unicauca.saberpro.domain.model.Pregunta;
import co.edu.unicauca.saberpro.domain.ports.inbound.CrearPreguntaComando;
import co.edu.unicauca.saberpro.domain.ports.inbound.CrearPreguntaComando.Opcion;
import co.edu.unicauca.saberpro.domain.ports.outbound.PreguntaRepositoryPort;
import co.edu.unicauca.saberpro.infrastructure.adapter.out.persistence.memory.InMemoryQuestionAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del servicio de aplicación sin Spring: el puerto de persistencia lo cubre el adaptador real
 * {@link InMemoryQuestionAdapter} y el de notificaciones un doble ({@link NotificacionPortFalso}).
 * El servicio no sabe cuál de los dos adaptadores de persistencia tiene detrás; por eso basta el de memoria.
 */
class CrearPreguntaServiceTest {

    private PreguntaRepositoryPort repositorio;
    private NotificacionPortFalso notificaciones;
    private CrearPreguntaService servicio;

    @BeforeEach
    void preparar() {
        repositorio = new InMemoryQuestionAdapter();
        notificaciones = new NotificacionPortFalso();
        servicio = new CrearPreguntaService(repositorio, notificaciones);
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
    void crearGuardaLaPreguntaEnBorradorEnElAdaptador() {
        Pregunta creada = servicio.crear(comando(cuatroOpcionesUnaClave()));

        assertNotNull(creada.getId());
        assertEquals(EstadoPregunta.BORRADOR, creada.getEstado());
        assertEquals(4, creada.getOpciones().size());
        assertTrue(repositorio.findById(creada.getId()).isPresent());
        assertTrue(notificaciones.nadaNotificado(), "crear no debe notificar");
    }

    @Test
    void crearConJustificacionCortaNoGuardaNada() {
        CrearPreguntaComando invalido = new CrearPreguntaComando("¿Qué es un puerto?", "Muy corta",
                cuatroOpcionesUnaClave());

        assertThrows(ReglaNegocioException.class, () -> servicio.crear(invalido));
        assertTrue(repositorio.findById(1L).isEmpty());
    }

    @Test
    void crearConCincoOpcionesNoGuardaNada() {
        List<Opcion> cinco = List.of(new Opcion("A", true), new Opcion("B", false), new Opcion("C", false),
                new Opcion("D", false), new Opcion("E", false));

        assertThrows(ReglaNegocioException.class, () -> servicio.crear(comando(cinco)));
        assertTrue(repositorio.findById(1L).isEmpty());
    }

    @Test
    void enviarARevisionCambiaEstadoPersisteYNotificaUnaVez() {
        Long id = servicio.crear(comando(cuatroOpcionesUnaClave())).getId();

        Pregunta enviada = servicio.enviarARevision(id);

        assertEquals(EstadoPregunta.PENDIENTE_REVISION, enviada.getEstado());
        assertEquals(EstadoPregunta.PENDIENTE_REVISION, repositorio.findById(id).orElseThrow().getEstado());
        assertEquals(1, notificaciones.notificadas().size());
        assertSame(enviada, notificaciones.notificadas().get(0));
    }

    @Test
    void enviarARevisionSinCuatroOpcionesNoCambiaEstadoNiNotifica() {
        Long id = servicio.crear(comando(List.of(new Opcion("Hexagonal", true), new Opcion("Singleton", false)))).getId();

        assertThrows(ReglaNegocioException.class, () -> servicio.enviarARevision(id));

        assertTrue(notificaciones.nadaNotificado());
        assertEquals(EstadoPregunta.BORRADOR, servicio.obtener(id).getEstado());
    }

    @Test
    void enviarARevisionConDosClavesNoCambiaEstadoNiNotifica() {
        Long id = servicio.crear(comando(List.of(new Opcion("A", true), new Opcion("B", true),
                new Opcion("C", false), new Opcion("D", false)))).getId();

        assertThrows(ReglaNegocioException.class, () -> servicio.enviarARevision(id));

        assertTrue(notificaciones.nadaNotificado());
        assertEquals(EstadoPregunta.BORRADOR, servicio.obtener(id).getEstado());
    }

    @Test
    void enviarARevisionDosVecesFallaLaSegunda() {
        Long id = servicio.crear(comando(cuatroOpcionesUnaClave())).getId();
        servicio.enviarARevision(id);

        assertThrows(ReglaNegocioException.class, () -> servicio.enviarARevision(id));
        assertEquals(1, notificaciones.notificadas().size());
    }

    @Test
    void preguntaInexistenteLanzaNoEncontrada() {
        assertThrows(PreguntaNoEncontradaException.class, () -> servicio.enviarARevision(99L));
        assertThrows(PreguntaNoEncontradaException.class, () -> servicio.obtener(99L));
        assertTrue(notificaciones.nadaNotificado());
    }
}
