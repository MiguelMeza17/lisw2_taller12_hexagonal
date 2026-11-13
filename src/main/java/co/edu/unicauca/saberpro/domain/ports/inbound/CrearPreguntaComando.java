package co.edu.unicauca.saberpro.domain.ports.inbound;

import java.util.List;

/**
 * Datos de entrada del caso de uso {@link CrearPreguntaUseCase#crear(CrearPreguntaComando)}.
 * Es Java puro: el adaptador web traduce su DTO HTTP a este comando.
 *
 * @param enunciado     texto del enunciado
 * @param justificacion justificación de la clave
 * @param opciones      opciones de respuesta en orden
 */
public record CrearPreguntaComando(String enunciado, String justificacion, List<Opcion> opciones) {

    /**
     * Copia defensiva para que el comando sea inmutable.
     */
    public CrearPreguntaComando {
        opciones = opciones == null ? List.of() : List.copyOf(opciones);
    }

    /**
     * Opción de respuesta dentro del comando.
     *
     * @param texto      texto de la opción
     * @param esCorrecta {@code true} si es la clave
     */
    public record Opcion(String texto, boolean esCorrecta) {
    }
}
