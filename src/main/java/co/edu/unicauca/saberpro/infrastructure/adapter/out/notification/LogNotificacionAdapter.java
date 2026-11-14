package co.edu.unicauca.saberpro.infrastructure.adapter.out.notification;

import co.edu.unicauca.saberpro.domain.model.Pregunta;
import co.edu.unicauca.saberpro.domain.ports.outbound.NotificacionPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adaptador de salida de notificaciones que deja constancia en el log. Puede reemplazarse por uno de correo
 * o de mensajería (RabbitMQ en la entrega final) sin cambiar el dominio ni el servicio de aplicación.
 */
public class LogNotificacionAdapter implements NotificacionPort {

    private static final Logger LOG = LoggerFactory.getLogger(LogNotificacionAdapter.class);

    @Override
    public void notificarEnvioARevision(Pregunta pregunta) {
        LOG.info("[NOTIFICACION] La pregunta {} quedó en estado {} y espera asignación de revisores.",
                pregunta.getId(), pregunta.getEstado());
    }
}
