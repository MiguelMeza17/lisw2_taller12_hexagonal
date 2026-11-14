package co.edu.unicauca.saberpro.domain.model;

import co.edu.unicauca.saberpro.domain.exception.ReglaNegocioException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas unitarias del objeto de valor {@link OpcionRespuesta} (Java puro, sin Spring).
 */
class OpcionRespuestaTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  "})
    void rechazaTextoNuloOVacio(String texto) {
        assertThrows(ReglaNegocioException.class, () -> new OpcionRespuesta(texto, true));
        assertThrows(ReglaNegocioException.class, () -> new OpcionRespuesta(texto, false));
    }

    @Test
    void distingueClaveDeDistractor() {
        OpcionRespuesta clave = new OpcionRespuesta("Hexagonal", true);
        OpcionRespuesta distractor = new OpcionRespuesta("Singleton", false);

        assertTrue(clave.isEsCorrecta());
        assertFalse(distractor.isEsCorrecta());
        assertEquals("Hexagonal", clave.getTexto());
    }

    @Test
    void igualdadPorValorIncluyeTextoYClave() {
        OpcionRespuesta a = new OpcionRespuesta("Hexagonal", true);
        OpcionRespuesta b = new OpcionRespuesta("Hexagonal", true);
        OpcionRespuesta mismoTextoDistractor = new OpcionRespuesta("Hexagonal", false);
        OpcionRespuesta otroTexto = new OpcionRespuesta("Singleton", true);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, mismoTextoDistractor);
        assertNotEquals(a, otroTexto);
    }

    @Test
    void esInmutable() {
        ValueObjects.assertInmutable(OpcionRespuesta.class);
    }
}
