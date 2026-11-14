package co.edu.unicauca.saberpro.infrastructure.adapter.in.web;

import co.edu.unicauca.saberpro.domain.exception.PreguntaNoEncontradaException;
import co.edu.unicauca.saberpro.domain.exception.ReglaNegocioException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

/**
 * Traduce las excepciones del dominio a respuestas HTTP.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    /**
     * Cuerpo de error.
     *
     * @param estado  código HTTP
     * @param error   tipo de error
     * @param mensaje detalle
     * @param fecha   momento del error
     */
    public record ErrorResponse(int estado, String error, String mensaje, Instant fecha) {
    }

    /** Regla de negocio o invariante incumplida → 400. */
    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorResponse> reglaNegocio(ReglaNegocioException e) {
        return respuesta(HttpStatus.BAD_REQUEST, "Regla de negocio", e.getMessage());
    }

    /** Pregunta inexistente → 404. */
    @ExceptionHandler(PreguntaNoEncontradaException.class)
    public ResponseEntity<ErrorResponse> noEncontrada(PreguntaNoEncontradaException e) {
        return respuesta(HttpStatus.NOT_FOUND, "No encontrada", e.getMessage());
    }

    /** JSON mal formado → 400. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> jsonInvalido(HttpMessageNotReadableException e) {
        return respuesta(HttpStatus.BAD_REQUEST, "JSON inválido", "El cuerpo de la petición no es un JSON válido.");
    }

    private static ResponseEntity<ErrorResponse> respuesta(HttpStatus estado, String error, String mensaje) {
        return ResponseEntity.status(estado).body(new ErrorResponse(estado.value(), error, mensaje, Instant.now()));
    }
}
