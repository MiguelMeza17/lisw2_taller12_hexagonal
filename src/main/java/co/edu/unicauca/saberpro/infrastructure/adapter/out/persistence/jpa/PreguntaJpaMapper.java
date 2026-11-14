package co.edu.unicauca.saberpro.infrastructure.adapter.out.persistence.jpa;

import co.edu.unicauca.saberpro.domain.model.Enunciado;
import co.edu.unicauca.saberpro.domain.model.Justificacion;
import co.edu.unicauca.saberpro.domain.model.OpcionRespuesta;
import co.edu.unicauca.saberpro.domain.model.Pregunta;

import java.util.List;

/**
 * Traduce entre el agregado {@link Pregunta} y su representación JPA.
 * <p>
 * Para rehidratar el agregado no se usa reflexión ni setters: se delega en {@link Pregunta#reconstituir},
 * que es la vía que el propio dominio ofrece para rehidratar una pregunta persistida volviendo a aplicar
 * sus invariantes (4 opciones y 1 clave si ya salió de borrador). Así la persistencia no puede producir
 * un agregado inválido.
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
        List<OpcionRespuesta> opciones = entidad.getOpciones().stream()
                .map(o -> new OpcionRespuesta(o.getTexto(), o.isEsCorrecta()))
                .toList();
        return Pregunta.reconstituir(entidad.getId(), new Enunciado(entidad.getEnunciado()),
                new Justificacion(entidad.getJustificacion()), opciones, entidad.getEstado());
    }
}
