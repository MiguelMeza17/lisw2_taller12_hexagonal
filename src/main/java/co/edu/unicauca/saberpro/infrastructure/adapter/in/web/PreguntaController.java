package co.edu.unicauca.saberpro.infrastructure.adapter.in.web;

import co.edu.unicauca.saberpro.domain.ports.inbound.ConsultarPreguntaUseCase;
import co.edu.unicauca.saberpro.domain.ports.inbound.CrearPreguntaUseCase;
import co.edu.unicauca.saberpro.infrastructure.adapter.in.web.dto.CrearPreguntaRequest;
import co.edu.unicauca.saberpro.infrastructure.adapter.in.web.dto.PreguntaResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador de entrada REST. Traduce HTTP a llamadas de los puertos de entrada; nunca usa repositorios
 * ni conoce qué adaptador de persistencia está activo.
 */
@RestController
@RequestMapping("/api/preguntas")
public class PreguntaController {

    private final CrearPreguntaUseCase crearPregunta;
    private final ConsultarPreguntaUseCase consultarPregunta;

    public PreguntaController(CrearPreguntaUseCase crearPregunta, ConsultarPreguntaUseCase consultarPregunta) {
        this.crearPregunta = crearPregunta;
        this.consultarPregunta = consultarPregunta;
    }

    /** Crea una pregunta en BORRADOR. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PreguntaResponse crear(@RequestBody CrearPreguntaRequest request) {
        return PreguntaResponse.de(crearPregunta.crear(request.aComando()));
    }

    /** Envía la pregunta a revisión (pasa a PENDIENTE_REVISION si cumple las invariantes). */
    @PostMapping("/{id}/enviar-revision")
    public PreguntaResponse enviarARevision(@PathVariable Long id) {
        return PreguntaResponse.de(crearPregunta.enviarARevision(id));
    }

    /** Consulta una pregunta. */
    @GetMapping("/{id}")
    public PreguntaResponse obtener(@PathVariable Long id) {
        return PreguntaResponse.de(consultarPregunta.obtener(id));
    }
}
