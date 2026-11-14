package co.edu.unicauca.saberpro.infrastructure.adapter.in.web.dto;

import co.edu.unicauca.saberpro.domain.model.Pregunta;

import java.util.List;

/**
 * Respuesta JSON con los datos de una pregunta. Evita exponer el agregado del dominio directamente.
 *
 * @param id            identificador
 * @param enunciado     texto del enunciado
 * @param justificacion justificación de la clave
 * @param estado        estado del ciclo de vida
 * @param opciones      opciones de respuesta
 */
public record PreguntaResponse(Long id, String enunciado, String justificacion, String estado,
                               List<CrearPreguntaRequest.OpcionDto> opciones) {

    /**
     * Construye la respuesta desde el agregado.
     *
     * @param p pregunta
     * @return DTO de respuesta
     */
    public static PreguntaResponse de(Pregunta p) {
        return new PreguntaResponse(p.getId(), p.getEnunciado().getTexto(), p.getJustificacion().getTexto(),
                p.getEstado().name(),
                p.getOpciones().stream().map(o -> new CrearPreguntaRequest.OpcionDto(o.getTexto(), o.isEsCorrecta())).toList());
    }
}
