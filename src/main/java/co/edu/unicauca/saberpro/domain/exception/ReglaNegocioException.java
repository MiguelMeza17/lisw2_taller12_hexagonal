package co.edu.unicauca.saberpro.domain.exception;

/**
 * Se lanza cuando una operación incumple una regla de negocio o una invariante del agregado.
 */
public class ReglaNegocioException extends DomainException {

    /**
     * Crea la excepción con la regla incumplida.
     *
     * @param mensaje descripción de la regla incumplida
     */
    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
