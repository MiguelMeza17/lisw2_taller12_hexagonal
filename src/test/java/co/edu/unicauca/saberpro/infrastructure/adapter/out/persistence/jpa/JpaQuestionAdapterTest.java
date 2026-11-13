package co.edu.unicauca.saberpro.infrastructure.adapter.out.persistence.jpa;

import co.edu.unicauca.saberpro.domain.model.EstadoPregunta;
import co.edu.unicauca.saberpro.domain.model.Enunciado;
import co.edu.unicauca.saberpro.domain.model.Justificacion;
import co.edu.unicauca.saberpro.domain.model.OpcionRespuesta;
import co.edu.unicauca.saberpro.domain.model.Pregunta;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueba el adaptador JPA contra H2 real.
 */
@DataJpaTest
class JpaQuestionAdapterTest {

    @Autowired
    private SpringDataPreguntaRepository springData;

    @Autowired
    private EntityManager em;

    private JpaQuestionAdapter adaptador;

    @BeforeEach
    void preparar() {
        adaptador = new JpaQuestionAdapter(springData);
    }

    private static Pregunta preguntaCompleta() {
        Pregunta p = new Pregunta(new Enunciado("¿Qué patrón aísla el dominio de la infraestructura?"),
                new Justificacion("Los puertos y adaptadores aíslan el núcleo."));
        p.agregarOpcion("Hexagonal", true);
        p.agregarOpcion("Singleton", false);
        p.agregarOpcion("Monolito", false);
        p.agregarOpcion("Capas acopladas", false);
        return p;
    }

    /** Fuerza a leer desde la base y no desde la caché de primer nivel. */
    private void limpiarContexto() {
        em.flush();
        em.clear();
    }

    @Test
    void guardarAsignaIdYPermiteRecuperarLaPregunta() {
        Pregunta guardada = adaptador.save(preguntaCompleta());
        limpiarContexto();

        assertNotNull(guardada.getId());
        Pregunta leida = adaptador.findById(guardada.getId()).orElseThrow();
        assertEquals(guardada.getEnunciado(), leida.getEnunciado());
        assertEquals(guardada.getJustificacion(), leida.getJustificacion());
        assertEquals(EstadoPregunta.BORRADOR, leida.getEstado());
        assertEquals(List.of(new OpcionRespuesta("Hexagonal", true), new OpcionRespuesta("Singleton", false),
                new OpcionRespuesta("Monolito", false), new OpcionRespuesta("Capas acopladas", false)), leida.getOpciones());
    }

    @Test
    void actualizarPersisteElCambioDeEstado() {
        Long id = adaptador.save(preguntaCompleta()).getId();
        limpiarContexto();

        Pregunta pregunta = adaptador.findById(id).orElseThrow();
        pregunta.enviarARevision();
        adaptador.save(pregunta);
        limpiarContexto();

        Pregunta leida = adaptador.findById(id).orElseThrow();
        assertEquals(EstadoPregunta.PENDIENTE_REVISION, leida.getEstado());
        assertEquals(4, leida.getOpciones().size());
        assertEquals(1, springData.count());
    }

    @Test
    void rehidrataEstadosAvanzadosDelCicloDeVida() {
        Pregunta pregunta = preguntaCompleta();
        pregunta.enviarARevision();
        pregunta.iniciarRevision();
        pregunta.aprobar();
        pregunta.publicar();
        Long id = adaptador.save(pregunta).getId();
        limpiarContexto();

        assertEquals(EstadoPregunta.PUBLICADA, adaptador.findById(id).orElseThrow().getEstado());
    }

    @Test
    void idInexistenteDevuelveVacio() {
        assertTrue(adaptador.findById(999L).isEmpty());
        assertTrue(adaptador.findById(null).isEmpty());
    }
}
