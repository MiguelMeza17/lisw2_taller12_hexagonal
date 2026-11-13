package co.edu.unicauca.saberpro.infrastructure.adapter.out.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Opción de respuesta persistida dentro de la tabla {@code opciones_respuesta}. Es un valor embebido
 * porque en el dominio {@code OpcionRespuesta} es un objeto de valor sin identidad propia.
 */
@Embeddable
public class OpcionRespuestaJpaEmbeddable {

    @Column(nullable = false, length = 1000)
    private String texto;

    @Column(name = "es_correcta", nullable = false)
    private boolean esCorrecta;

    protected OpcionRespuestaJpaEmbeddable() {
        // requerido por JPA
    }

    OpcionRespuestaJpaEmbeddable(String texto, boolean esCorrecta) {
        this.texto = texto;
        this.esCorrecta = esCorrecta;
    }

    public String getTexto() {
        return texto;
    }

    public boolean isEsCorrecta() {
        return esCorrecta;
    }
}
