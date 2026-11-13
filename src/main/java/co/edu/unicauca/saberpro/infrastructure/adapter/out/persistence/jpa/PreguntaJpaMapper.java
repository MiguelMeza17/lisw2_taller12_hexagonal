package co.edu.unicauca.saberpro.infrastructure.adapter.out.persistence.jpa;

import co.edu.unicauca.saberpro.domain.model.Enunciado;
import co.edu.unicauca.saberpro.domain.model.EstadoPregunta;
import co.edu.unicauca.saberpro.domain.model.Justificacion;
import co.edu.unicauca.saberpro.domain.model.Pregunta;

import java.util.List;

/**
 * Traduce entre el agregado {@link Pregunta} y su representación JPA.
 * <p>
 * Para rehidratar el agregado no se fuerza el estado con reflexión ni setters: se reconstruye en BORRADOR
 * y se repiten las transiciones de su ciclo de vida hasta el estado guardado. Así el agregado vuelve a pasar
 * por sus propias invariantes y el dominio no necesita ningún método especial para la persistencia.
 */
final class PreguntaJpaMapper {

    private PreguntaJpaMapper() {
    }

    static PreguntaJpaEntity aEntidad(Pregunta pregunta) {
        List<OpcionRespuestaJpaEmbeddable> opciones = pregunta.getOpciones().stream()
                .map(o -> new OpcionRespuestaJpaEmbeddable(o.getTexto(), o.isEsCorrecta()))
                .toList();
        return new PreguntaJpaEntity(pregunta.getId(), pregunta.getEnunciado().getTexto(),
                pregunta.getJustificacion().getTexto(), pregunta.getEstado(), opciones);
    }

    static Pregunta aDominio(PreguntaJpaEntity entidad) {
        Pregunta pregunta = new Pregunta(new Enunciado(entidad.getEnunciado()), new Justificacion(entidad.getJustificacion()));
        entidad.getOpciones().forEach(o -> pregunta.agregarOpcion(o.getTexto(), o.isEsCorrecta()));
        pregunta.asignarId(entidad.getId());
        avanzarHasta(pregunta, entidad.getEstado());
        return pregunta;
    }

    private static void avanzarHasta(Pregunta pregunta, EstadoPregunta destino) {
        switch (destino) {
            case BORRADOR -> {
            }
            case PENDIENTE_REVISION -> pregunta.enviarARevision();
            case EN_REVISION -> {
                avanzarHasta(pregunta, EstadoPregunta.PENDIENTE_REVISION);
                pregunta.iniciarRevision();
            }
            case APROBADA -> {
                avanzarHasta(pregunta, EstadoPregunta.EN_REVISION);
                pregunta.aprobar();
            }
            case PUBLICADA -> {
                avanzarHasta(pregunta, EstadoPregunta.APROBADA);
                pregunta.publicar();
            }
            case ARCHIVADA -> {
                avanzarHasta(pregunta, EstadoPregunta.PUBLICADA);
                pregunta.archivar();
            }
        }
    }
}
