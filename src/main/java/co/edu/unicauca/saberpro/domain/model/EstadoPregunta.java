package co.edu.unicauca.saberpro.domain.model;

import java.util.EnumSet;
import java.util.Set;

/**
 * Estados del ciclo de vida de una pregunta (contexto Ciclo de Vida):
 * BORRADOR → PENDIENTE_REVISION → EN_REVISION → APROBADA → PUBLICADA → ARCHIVADA.
 * Una pregunta pendiente o en revisión puede ser rechazada y vuelve a BORRADOR para su corrección.
 */
public enum EstadoPregunta {
    BORRADOR,
    PENDIENTE_REVISION,
    EN_REVISION,
    APROBADA,
    PUBLICADA,
    ARCHIVADA;

    /**
     * Estados a los que se puede pasar desde este.
     *
     * @return transiciones permitidas
     */
    public Set<EstadoPregunta> transicionesPermitidas() {
        return switch (this) {
            case BORRADOR -> EnumSet.of(PENDIENTE_REVISION);
            case PENDIENTE_REVISION -> EnumSet.of(EN_REVISION, BORRADOR);
            case EN_REVISION -> EnumSet.of(APROBADA, BORRADOR);
            case APROBADA -> EnumSet.of(PUBLICADA);
            case PUBLICADA -> EnumSet.of(ARCHIVADA);
            case ARCHIVADA -> EnumSet.noneOf(EstadoPregunta.class);
        };
    }

    /**
     * Indica si la transición al estado destino es válida.
     *
     * @param destino estado deseado
     * @return {@code true} si la transición está permitida
     */
    public boolean puedeTransitarA(EstadoPregunta destino) {
        return transicionesPermitidas().contains(destino);
    }
}
