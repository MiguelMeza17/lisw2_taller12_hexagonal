package co.edu.unicauca.saberpro.application.service;

import co.edu.unicauca.saberpro.domain.model.Pregunta;
import co.edu.unicauca.saberpro.domain.ports.outbound.NotificacionPort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Doble de prueba (fake) del puerto de salida {@link NotificacionPort}: en vez de notificar,
 * registra las preguntas recibidas para que las pruebas verifiquen qué se notificó y cuántas veces.
 */
final class NotificacionPortFalso implements NotificacionPort {

    private final List<Pregunta> notificadas = new ArrayList<>();

    @Override
    public void notificarEnvioARevision(Pregunta pregunta) {
        notificadas.add(pregunta);
    }

    List<Pregunta> notificadas() {
        return Collections.unmodifiableList(notificadas);
    }

    boolean nadaNotificado() {
        return notificadas.isEmpty();
    }
}
