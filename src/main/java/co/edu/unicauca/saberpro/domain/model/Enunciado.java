package co.edu.unicauca.saberpro.domain.model;

import co.edu.unicauca.saberpro.domain.exception.ReglaNegocioException;

import java.util.Objects;

/**
 * Objeto de valor inmutable con el texto que plantea la pregunta.
 * Invariante: no puede ser nulo ni estar vacío.
 */
public final class Enunciado {

    private final String texto;

    /**
     * Crea un enunciado válido.
     *
     * @param texto texto del enunciado
     * @throws ReglaNegocioException si el texto es nulo o vacío
     */
    public Enunciado(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new ReglaNegocioException("El enunciado no puede estar vacío.");
        }
        this.texto = texto.trim();
    }

    public String getTexto() {
        return texto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Enunciado otro)) {
            return false;
        }
        return texto.equals(otro.texto);
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
