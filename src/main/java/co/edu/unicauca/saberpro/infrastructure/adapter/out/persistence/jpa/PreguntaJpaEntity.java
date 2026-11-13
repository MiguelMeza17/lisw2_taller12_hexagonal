package co.edu.unicauca.saberpro.infrastructure.adapter.out.persistence.jpa;

import co.edu.unicauca.saberpro.domain.model.EstadoPregunta;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * Representación JPA de una pregunta. Vive solo en infraestructura: el dominio no conoce esta clase
 * ni ninguna anotación de persistencia.
 */
@Entity
@Table(name = "preguntas")
public class PreguntaJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2000)
    private String enunciado;

    @Column(nullable = false, length = 2000)
    private String justificacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoPregunta estado;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "opciones_respuesta", joinColumns = @JoinColumn(name = "pregunta_id"))
    @OrderColumn(name = "orden")
    private List<OpcionRespuestaJpaEmbeddable> opciones = new ArrayList<>();

    protected PreguntaJpaEntity() {
        // requerido por JPA
    }

    PreguntaJpaEntity(Long id, String enunciado, String justificacion, EstadoPregunta estado,
                      List<OpcionRespuestaJpaEmbeddable> opciones) {
        this.id = id;
        this.enunciado = enunciado;
        this.justificacion = justificacion;
        this.estado = estado;
        this.opciones = new ArrayList<>(opciones);
    }

    public Long getId() {
        return id;
    }

    public String getEnunciado() {
        return enunciado;
    }

    public String getJustificacion() {
        return justificacion;
    }

    public EstadoPregunta getEstado() {
        return estado;
    }

    public List<OpcionRespuestaJpaEmbeddable> getOpciones() {
        return opciones;
    }
}
