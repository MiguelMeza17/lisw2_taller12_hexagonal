package co.edu.unicauca.saberpro.infrastructure.adapter.in.web.dto;

import co.edu.unicauca.saberpro.domain.ports.inbound.CrearPreguntaComando;

import java.util.List;

/**
 * Cuerpo JSON de {@code POST /api/preguntas}.
 *
 * @param enunciado     texto del enunciado
 * @param justificacion justificación de la clave (mínimo 15 caracteres)
 * @param opciones      opciones de respuesta
 */
public record CrearPreguntaRequest(String enunciado, String justificacion, List<OpcionDto> opciones) {

    /**
     * Opción de respuesta en el JSON.
     *
     * @param texto      texto de la opción
     * @param esCorrecta {@code true} si es la clave
     */
    public record OpcionDto(String texto, boolean esCorrecta) {
    }

    /**
     * Traduce el DTO HTTP al comando del puerto de entrada.
     *
     * @return comando en Java puro
     */
    public CrearPreguntaComando aComando() {
        List<CrearPreguntaComando.Opcion> lista = opciones == null ? List.of()
                : opciones.stream().map(o -> new CrearPreguntaComando.Opcion(o.texto(), o.esCorrecta())).toList();
        return new CrearPreguntaComando(enunciado, justificacion, lista);
    }
}
