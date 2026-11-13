package co.edu.unicauca.saberpro.domain.model;

import co.edu.unicauca.saberpro.domain.exception.ReglaNegocioException;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Objeto de valor inmutable que explica por qué la clave es la respuesta correcta.
 * <p>
 * Invariantes:
 * <ul>
 *     <li>Debe contener al menos {@value #LONGITUD_MINIMA} caracteres (sin contar espacios de los extremos).</li>
 *     <li>No puede contener expresiones prohibidas: palabras que afirman sin explicar
 *     (por ejemplo "obviamente") o que remiten a otras opciones en lugar de justificar.</li>
 * </ul>
 */
public final class Justificacion {

    /** Longitud mínima de la justificación. */
    public static final int LONGITUD_MINIMA = 15;

    /** Expresiones que no aportan una justificación pedagógica (se comparan sin tildes ni mayúsculas). */
    public static final List<String> PALABRAS_PROHIBIDAS = List.of(
            "obviamente", "obvio", "evidentemente", "es evidente", "claramente",
            "todas las anteriores", "ninguna de las anteriores", "porque si");

    private final String texto;

    /**
     * Crea una justificación válida.
     *
     * @param texto texto de la justificación
     * @throws ReglaNegocioException si es muy corta o contiene expresiones prohibidas
     */
    public Justificacion(String texto) {
        if (texto == null || texto.trim().length() < LONGITUD_MINIMA) {
            throw new ReglaNegocioException("La justificación debe contener al menos " + LONGITUD_MINIMA + " caracteres.");
        }
        String normalizado = normalizar(texto);
        for (String prohibida : PALABRAS_PROHIBIDAS) {
            if (normalizado.matches(".*\\b" + prohibida + "\\b.*")) {
                throw new ReglaNegocioException("La justificación contiene una expresión prohibida: \"" + prohibida + "\".");
            }
        }
        this.texto = texto.trim();
    }

    private static String normalizar(String texto) {
        String sinTildes = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sinTildes.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }

    public String getTexto() {
        return texto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Justificacion otra)) {
            return false;
        }
        return texto.equals(otra.texto);
    }

    @Override
    public int hashCode() {
        return Objects.hash(texto);
    }

    @Override
    public String toString() {
        return texto;
    }
}
