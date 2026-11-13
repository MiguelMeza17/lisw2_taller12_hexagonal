package co.edu.unicauca.saberpro.domain.model;

import co.edu.unicauca.saberpro.domain.exception.ReglaNegocioException;

import java.util.Objects;

/**
 * Objeto de valor inmutable que representa una opción de respuesta: su texto y si es la clave
 * (respuesta correcta) o un distractor.
 */
public final class OpcionRespuesta {

    private final String texto;
    private final boolean esCorrecta;

    /**
     * Crea una opción de respuesta.
     *
     * @param texto      texto de la opción
     * @param esCorrecta {@code true} si es la clave
     * @throws ReglaNegocioException si el texto es nulo o vacío
     */
    public OpcionRespuesta(String texto, boolean esCorrecta) {
        if (texto == null || texto.isBlank()) {
            throw new ReglaNegocioException("El texto de la opción no puede estar vacío.");
        }
        this.texto = texto.trim();
        this.esCorrecta = esCorrecta;
    }

    public String getTexto() {
        return texto;
    }

    public boolean isEsCorrecta() {
        return esCorrecta;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OpcionRespuesta otra)) {
            return false;
        }
        return esCorrecta == otra.esCorrecta && texto.equals(otra.texto);
    }

    @Override
    public int hashCode() {
        return Objects.hash(texto, esCorrecta);
    }

    @Override
    public String toString() {
        return (esCorrecta ? "[clave] " : "") + texto;
    }
}
