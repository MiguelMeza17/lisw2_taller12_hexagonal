package co.edu.unicauca.saberpro.domain.exception;

/**
 * Clase base de todas las excepciones del dominio.
 */
public class DomainException extends RuntimeException {

    /**
     * Crea la excepción con un mensaje descriptivo.
     *
     * @param mensaje explicación del problema
     */
    public DomainException(String mensaje) {
        super(mensaje);
    }
}
