package co.edu.unicauca.saberpro.domain.ports.inbound;

import co.edu.unicauca.saberpro.domain.exception.PreguntaNoEncontradaException;
import co.edu.unicauca.saberpro.domain.model.Pregunta;

/**
 * Puerto de entrada (driving): consulta de una pregunta por su identificador.
 * Se separa de {@link CrearPreguntaUseCase} para que cada puerto tenga una sola responsabilidad.
 */
public interface ConsultarPreguntaUseCase {

    /**
     * Obtiene una pregunta.
     *
     * @param id identificador
     * @return la pregunta
     * @throws PreguntaNoEncontradaException si no existe
     */
    Pregunta obtener(Long id);
}
