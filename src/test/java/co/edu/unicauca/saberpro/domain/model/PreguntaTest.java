package co.edu.unicauca.saberpro.domain.model;

import co.edu.unicauca.saberpro.domain.exception.ReglaNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas unitarias de la raíz de agregado {@link Pregunta}, en Java puro y sin Spring:
 * invariantes, ciclo de vida, encapsulamiento de las opciones y reconstitución.
 */
class PreguntaTest {

    private static final Enunciado ENUNCIADO = new Enunciado("¿Qué patrón aísla el dominio de la infraestructura?");
    private static final Justificacion JUSTIFICACION = new Justificacion("Los puertos y adaptadores aíslan el núcleo.");

    private Pregunta pregunta;

    @BeforeEach
    void preparar() {
        pregunta = new Pregunta(ENUNCIADO, JUSTIFICACION);
    }

    private static void agregarOpciones(Pregunta p, int cantidad, int claves) {
        for (int i = 1; i <= cantidad; i++) {
            p.agregarOpcion("Opcion " + i, i <= claves);
        }
    }

    @Nested
    class CicloDeVida {

        @Test
        void iniciaEnBorradorSinIdentificador() {
            assertEquals(EstadoPregunta.BORRADOR, pregunta.getEstado());
            assertNull(pregunta.getId());
            assertTrue(pregunta.getOpciones().isEmpty());
        }

        @Test
        void conCuatroOpcionesYUnaClavePasaAPendienteRevision() {
            agregarOpciones(pregunta, 4, 1);

            pregunta.enviarARevision();

            assertEquals(EstadoPregunta.PENDIENTE_REVISION, pregunta.getEstado());
        }

        @Test
        void noSePuedeEnviarDosVecesARevision() {
            agregarOpciones(pregunta, 4, 1);
            pregunta.enviarARevision();

            assertThrows(ReglaNegocioException.class, pregunta::enviarARevision);
        }

        @Test
        void fueraDeBorradorNoSeModificanLasOpciones() {
            agregarOpciones(pregunta, 4, 1);
            pregunta.enviarARevision();

            assertThrows(ReglaNegocioException.class, () -> pregunta.agregarOpcion("Otra", false));
            assertThrows(ReglaNegocioException.class, pregunta::quitarOpciones);
            assertEquals(4, pregunta.getOpciones().size());
        }

        @Test
        void rechazarDevuelveABorrador() {
            agregarOpciones(pregunta, 4, 1);
            pregunta.enviarARevision();
            pregunta.iniciarRevision();

            pregunta.rechazar();

            assertEquals(EstadoPregunta.BORRADOR, pregunta.getEstado());
        }

        @Test
        void transicionNoPermitidaLanzaExcepcion() {
            assertThrows(ReglaNegocioException.class, pregunta::aprobar);
            assertThrows(ReglaNegocioException.class, pregunta::publicar);
            assertEquals(EstadoPregunta.BORRADOR, pregunta.getEstado());
        }
    }

    @Nested
    class InvariantesParaEnviarARevision {

        @Test
        void conTresOpcionesNoSeEnvia() {
            agregarOpciones(pregunta, 3, 1);

            ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, pregunta::enviarARevision);

            assertTrue(ex.getMessage().contains("4"));
            assertEquals(EstadoPregunta.BORRADOR, pregunta.getEstado());
        }

        @Test
        void noAdmiteUnaQuintaOpcion() {
            agregarOpciones(pregunta, 4, 1);

            assertThrows(ReglaNegocioException.class, () -> pregunta.agregarOpcion("Opcion 5", false));
            assertEquals(4, pregunta.getOpciones().size());
        }

        @Test
        void sinClaveCorrectaNoSeEnvia() {
            agregarOpciones(pregunta, 4, 0);

            ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, pregunta::enviarARevision);

            assertTrue(ex.getMessage().contains("1 opción correcta"));
            assertEquals(EstadoPregunta.BORRADOR, pregunta.getEstado());
        }

        @Test
        void conDosClavesNoSeEnvia() {
            agregarOpciones(pregunta, 4, 2);

            assertThrows(ReglaNegocioException.class, pregunta::enviarARevision);
            assertEquals(EstadoPregunta.BORRADOR, pregunta.getEstado());
        }

        @Test
        void noAdmiteOpcionesRepetidas() {
            pregunta.agregarOpcion("Hexagonal", true);

            assertThrows(ReglaNegocioException.class, () -> pregunta.agregarOpcion("hexagonal", false));
        }

        @Test
        void sinEnunciadoOJustificacionNoSeEnvia() {
            Pregunta vacia = new Pregunta();
            agregarOpciones(vacia, 4, 1);

            assertThrows(ReglaNegocioException.class, vacia::enviarARevision);
        }
    }

    @Nested
    class Encapsulamiento {

        @Test
        void laListaDeOpcionesNoSePuedeModificarDesdeAfuera() {
            agregarOpciones(pregunta, 2, 1);
            List<OpcionRespuesta> opciones = pregunta.getOpciones();

            assertThrows(UnsupportedOperationException.class, () -> opciones.add(new OpcionRespuesta("Intrusa", false)));
            assertThrows(UnsupportedOperationException.class, opciones::clear);
            assertEquals(2, pregunta.getOpciones().size());
        }

        @Test
        void elIdentificadorSeAsignaUnaSolaVez() {
            pregunta.asignarId(7L);

            assertThrows(ReglaNegocioException.class, () -> pregunta.asignarId(8L));
            assertEquals(7L, pregunta.getId());
        }
    }

    @Nested
    class Reconstitucion {

        private final List<OpcionRespuesta> cuatroConUnaClave = List.of(
                new OpcionRespuesta("Hexagonal", true), new OpcionRespuesta("Singleton", false),
                new OpcionRespuesta("Monolito", false), new OpcionRespuesta("Capas acopladas", false));

        @Test
        void rehidrataConIdEstadoYOpcionesGuardados() {
            Pregunta rehidratada = Pregunta.reconstituir(42L, ENUNCIADO, JUSTIFICACION, cuatroConUnaClave,
                    EstadoPregunta.APROBADA);

            assertEquals(42L, rehidratada.getId());
            assertEquals(EstadoPregunta.APROBADA, rehidratada.getEstado());
            assertEquals(cuatroConUnaClave, rehidratada.getOpciones());
            assertEquals(ENUNCIADO, rehidratada.getEnunciado());
            assertEquals(JUSTIFICACION, rehidratada.getJustificacion());
            assertTrue(rehidratada.esUtilizableEnSimulacro());
        }

        @Test
        void unBorradorIncompletoSeRehidrataTalCual() {
            Pregunta borrador = Pregunta.reconstituir(1L, ENUNCIADO, JUSTIFICACION,
                    List.of(new OpcionRespuesta("Hexagonal", true)), EstadoPregunta.BORRADOR);

            assertEquals(EstadoPregunta.BORRADOR, borrador.getEstado());
            assertEquals(1, borrador.getOpciones().size());
        }

        @Test
        void noRehidrataFueraDeBorradorSiIncumpleLasInvariantes() {
            List<OpcionRespuesta> tresOpciones = cuatroConUnaClave.subList(0, 3);
            List<OpcionRespuesta> dosClaves = List.of(
                    new OpcionRespuesta("A", true), new OpcionRespuesta("B", true),
                    new OpcionRespuesta("C", false), new OpcionRespuesta("D", false));

            assertThrows(ReglaNegocioException.class, () -> Pregunta.reconstituir(1L, ENUNCIADO, JUSTIFICACION,
                    tresOpciones, EstadoPregunta.PENDIENTE_REVISION));
            assertThrows(ReglaNegocioException.class, () -> Pregunta.reconstituir(1L, ENUNCIADO, JUSTIFICACION,
                    dosClaves, EstadoPregunta.PUBLICADA));
        }

        @Test
        void exigeIdentificadorYEstado() {
            assertThrows(ReglaNegocioException.class, () -> Pregunta.reconstituir(null, ENUNCIADO, JUSTIFICACION,
                    cuatroConUnaClave, EstadoPregunta.BORRADOR));
            assertThrows(ReglaNegocioException.class, () -> Pregunta.reconstituir(1L, ENUNCIADO, JUSTIFICACION,
                    cuatroConUnaClave, null));
        }

        @Test
        void laPreguntaRehidratadaSigueSuCicloDeVida() {
            Pregunta pendiente = Pregunta.reconstituir(5L, ENUNCIADO, JUSTIFICACION, cuatroConUnaClave,
                    EstadoPregunta.PENDIENTE_REVISION);

            pendiente.iniciarRevision();
            pendiente.aprobar();

            assertEquals(EstadoPregunta.APROBADA, pendiente.getEstado());
            assertThrows(ReglaNegocioException.class, () -> pendiente.agregarOpcion("Otra", false));
        }
    }
}
