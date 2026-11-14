package co.edu.unicauca.saberpro.infrastructure.adapter.out.persistence.memory;

import co.edu.unicauca.saberpro.domain.model.Enunciado;
import co.edu.unicauca.saberpro.domain.model.EstadoPregunta;
import co.edu.unicauca.saberpro.domain.model.Justificacion;
import co.edu.unicauca.saberpro.domain.model.Pregunta;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryQuestionAdapterTest {

    private final InMemoryQuestionAdapter adaptador = new InMemoryQuestionAdapter();

    private static Pregunta pregunta() {
        Pregunta p = new Pregunta(new Enunciado("¿Qué patrón aísla el dominio?"),
                new Justificacion("Los puertos y adaptadores aíslan el núcleo."));
        p.agregarOpcion("Hexagonal", true);
        p.agregarOpcion("Singleton", false);
        p.agregarOpcion("Monolito", false);
        p.agregarOpcion("Capas acopladas", false);
        return p;
    }

    @Test
    void guardarAsignaIdsDistintosYRecupera() {
        Pregunta a = adaptador.save(pregunta());
        Pregunta b = adaptador.save(pregunta());

        assertNotEquals(a.getId(), b.getId());
        assertSame(a, adaptador.findById(a.getId()).orElseThrow());
    }

    @Test
    void guardarDeNuevoActualizaSinCambiarElId() {
        Pregunta p = adaptador.save(pregunta());
        Long id = p.getId();
        p.enviarARevision();

        adaptador.save(p);

        assertEquals(id, p.getId());
        assertEquals(EstadoPregunta.PENDIENTE_REVISION, adaptador.findById(id).orElseThrow().getEstado());
    }

    @Test
    void idInexistenteDevuelveVacio() {
        assertTrue(adaptador.findById(42L).isEmpty());
        assertTrue(adaptador.findById(null).isEmpty());
    }
}
