package co.edu.unicauca.saberpro.domain.ports.inbound;

import co.edu.unicauca.saberpro.domain.exception.PreguntaNoEncontradaException;
import co.edu.unicauca.saberpro.domain.exception.ReglaNegocioException;
import co.edu.unicauca.saberpro.domain.model.Pregunta;

/**
 * Puerto de entrada (driving): creación de preguntas y su envío a revisión.
 * <p>
 * Los adaptadores de entrada (por ejemplo, el controlador REST) solo conocen esta interfaz,
 * nunca la implementación ni los repositorios.
 */
public interface CrearPreguntaUseCase {

    /**
     * Crea una pregunta en estado BORRADOR con su enunciado, justificación y opciones.
     *
     * @param comando datos de la pregunta
     * @return la pregunta creada, con identificador
     * @throws ReglaNegocioException si los datos incumplen una regla del dominio
     */
    Pregunta crear(CrearPreguntaComando comando);

    /**
     * Envía a revisión una pregunta en BORRADOR. El agregado valida que tenga exactamente
     * 4 opciones y 1 sola clave antes de pasar a PENDIENTE_REVISION.
     *
     * @param id identificador de la pregunta
     * @return la pregunta en estado PENDIENTE_REVISION
     * @throws PreguntaNoEncontradaException si no existe
     * @throws ReglaNegocioException         si no cumple las invariantes
     */
    Pregunta enviarARevision(Long id);
}
