package co.edu.unicauca.saberpro.application.service;

import co.edu.unicauca.saberpro.domain.exception.PreguntaNoEncontradaException;
import co.edu.unicauca.saberpro.domain.model.Enunciado;
import co.edu.unicauca.saberpro.domain.model.Justificacion;
import co.edu.unicauca.saberpro.domain.model.Pregunta;
import co.edu.unicauca.saberpro.domain.ports.inbound.ConsultarPreguntaUseCase;
import co.edu.unicauca.saberpro.domain.ports.inbound.CrearPreguntaComando;
import co.edu.unicauca.saberpro.domain.ports.inbound.CrearPreguntaUseCase;
import co.edu.unicauca.saberpro.domain.ports.outbound.NotificacionPort;
import co.edu.unicauca.saberpro.domain.ports.outbound.PreguntaRepositoryPort;

import java.util.Objects;

/**
 * Servicio de aplicación que implementa los puertos de entrada de preguntas.
 * <p>
 * Solo orquesta: las reglas de negocio las aplica el agregado {@link Pregunta}. Depende únicamente de los
 * puertos de salida, recibidos por constructor, así que no sabe si la persistencia es JPA o en memoria.
 * No lleva anotaciones de Spring; la infraestructura lo registra como bean.
 */
public class CrearPreguntaService implements CrearPreguntaUseCase, ConsultarPreguntaUseCase {

    private final PreguntaRepositoryPort repositorio;
    private final NotificacionPort notificaciones;

    /**
     * Crea el servicio con sus puertos de salida.
     *
     * @param repositorio    persistencia de preguntas
     * @param notificaciones envío de notificaciones
     */
    public CrearPreguntaService(PreguntaRepositoryPort repositorio, NotificacionPort notificaciones) {
        this.repositorio = Objects.requireNonNull(repositorio, "repositorio");
        this.notificaciones = Objects.requireNonNull(notificaciones, "notificaciones");
    }

    @Override
    public Pregunta crear(CrearPreguntaComando comando) {
        Objects.requireNonNull(comando, "comando");
        Pregunta pregunta = new Pregunta(new Enunciado(comando.enunciado()), new Justificacion(comando.justificacion()));
        comando.opciones().forEach(o -> pregunta.agregarOpcion(o.texto(), o.esCorrecta()));
        return repositorio.save(pregunta);
    }

    @Override
    public Pregunta enviarARevision(Long id) {
        Pregunta pregunta = obtener(id);
        pregunta.enviarARevision();
        Pregunta guardada = repositorio.save(pregunta);
        notificaciones.notificarEnvioARevision(guardada);
        return guardada;
    }

    @Override
    public Pregunta obtener(Long id) {
        return repositorio.findById(id).orElseThrow(() -> new PreguntaNoEncontradaException(id));
    }
}
