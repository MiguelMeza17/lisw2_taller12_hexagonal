package co.edu.unicauca.saberpro.domain.model;

import co.edu.unicauca.saberpro.domain.exception.DomainException;
import co.edu.unicauca.saberpro.domain.exception.ReglaNegocioException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas unitarias del objeto de valor {@link Enunciado} (Java puro, sin Spring).
 */
class EnunciadoTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t\n"})
    void rechazaTextoNuloOVacio(String texto) {
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () -> new Enunciado(texto));

        assertTrue(ex.getMessage().contains("enunciado"));
        assertInstanceOf(DomainException.class, ex, "ReglaNegocioException debe ser una DomainException");
    }

    @Test
    void conservaElTextoSinEspaciosEnLosExtremos() {
        Enunciado enunciado = new Enunciado("  ¿Qué patrón aísla el dominio de la infraestructura?  ");

        assertEquals("¿Qué patrón aísla el dominio de la infraestructura?", enunciado.getTexto());
    }

    @Test
    void igualdadPorValor() {
        Enunciado a = new Enunciado("¿Qué es un puerto?");
        Enunciado b = new Enunciado("¿Qué es un puerto?");
        Enunciado c = new Enunciado("¿Qué es un adaptador?");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
    }

    @Test
    void esInmutable() {
        ValueObjects.assertInmutable(Enunciado.class);
    }
}
