package co.edu.unicauca.saberpro.domain.exception;

/**
 * Se lanza cuando se busca una pregunta que no existe.
 */
public class PreguntaNoEncontradaException extends DomainException {

    /**
     * Crea la excepción para el identificador buscado.
     *
     * @param id identificador que no existe
     */
    public PreguntaNoEncontradaException(Long id) {
        super("No existe la pregunta con id " + id + ".");
    }
}
