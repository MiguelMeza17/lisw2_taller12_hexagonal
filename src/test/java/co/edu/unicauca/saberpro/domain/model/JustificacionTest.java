package co.edu.unicauca.saberpro.domain.model;

import co.edu.unicauca.saberpro.domain.exception.ReglaNegocioException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas unitarias del objeto de valor {@link Justificacion} (Java puro, sin Spring).
 */
class JustificacionTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"Muy corta", "Catorce letras", "    Catorce letras    "})
    void rechazaMenosDeQuinceCaracteres(String texto) {
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () -> new Justificacion(texto));

        assertTrue(ex.getMessage().contains(String.valueOf(Justificacion.LONGITUD_MINIMA)));
    }

    @Test
    void aceptaExactamenteQuinceCaracteres() {
        String quince = "Quince letras..";
        assertEquals(Justificacion.LONGITUD_MINIMA, quince.length());

        assertEquals(quince, new Justificacion(quince).getTexto());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Obviamente es la respuesta correcta",
            "Es la correcta porque si y punto",
            "Es claramente la mejor opcion posible",
            "Todas las anteriores aplican aqui"})
    void rechazaExpresionesQueNoJustifican(String texto) {
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () -> new Justificacion(texto));

        assertTrue(ex.getMessage().contains("prohibida"));
    }

    @Test
    void igualdadPorValor() {
        Justificacion a = new Justificacion("Los puertos aíslan el núcleo de la tecnología.");
        Justificacion b = new Justificacion("Los puertos aíslan el núcleo de la tecnología.");
        Justificacion c = new Justificacion("Los adaptadores implementan los puertos.");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
    }

    @Test
    void esInmutable() {
        ValueObjects.assertInmutable(Justificacion.class);
    }
}
