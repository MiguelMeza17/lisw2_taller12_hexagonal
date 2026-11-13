package co.edu.unicauca.saberpro.infrastructure.adapter.out.persistence.jpa;

import co.edu.unicauca.saberpro.domain.model.Pregunta;
import co.edu.unicauca.saberpro.domain.ports.outbound.PreguntaRepositoryPort;

import java.util.Objects;
import java.util.Optional;

/**
 * Adaptador de salida que implementa {@link PreguntaRepositoryPort} con Spring Data JPA sobre H2.
 * No lleva anotaciones de Spring: la configuración decide si se registra como bean, lo que permite
 * intercambiarlo por el adaptador en memoria sin tocar el dominio.
 */
public class JpaQuestionAdapter implements PreguntaRepositoryPort {

    private final SpringDataPreguntaRepository repositorio;

    public JpaQuestionAdapter(SpringDataPreguntaRepository repositorio) {
        this.repositorio = Objects.requireNonNull(repositorio, "repositorio");
    }

    @Override
    public Pregunta save(Pregunta pregunta) {
        PreguntaJpaEntity guardada = repositorio.save(PreguntaJpaMapper.aEntidad(pregunta));
        if (pregunta.getId() == null) {
            pregunta.asignarId(guardada.getId());
        }
        return pregunta;
    }

    @Override
    public Optional<Pregunta> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return repositorio.findById(id).map(PreguntaJpaMapper::aDominio);
    }
}
