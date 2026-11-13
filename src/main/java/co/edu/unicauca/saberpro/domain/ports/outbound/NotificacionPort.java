package co.edu.unicauca.saberpro.domain.ports.outbound;

import co.edu.unicauca.saberpro.domain.model.Pregunta;

/**
 * Puerto de salida (driven) de notificaciones. El hexágono avisa que algo ocurrió
 * sin saber si la notificación va al log, a un correo o a un bus de eventos.
 */
public interface NotificacionPort {

    /**
     * Notifica que una pregunta quedó pendiente de revisión.
     *
     * @param pregunta pregunta enviada a revisión
     */
    void notificarEnvioARevision(Pregunta pregunta);
}
