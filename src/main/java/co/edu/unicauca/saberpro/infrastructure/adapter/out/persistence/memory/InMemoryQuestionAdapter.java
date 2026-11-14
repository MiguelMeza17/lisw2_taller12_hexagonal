package co.edu.unicauca.saberpro.infrastructure.adapter.out.persistence.memory;

import co.edu.unicauca.saberpro.domain.model.Pregunta;
import co.edu.unicauca.saberpro.domain.ports.outbound.PreguntaRepositoryPort;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Adaptador de salida que implementa {@link PreguntaRepositoryPort} sobre un {@code HashMap} en memoria.
 * Tiene el mismo comportamiento observable que {@code JpaQuestionAdapter}: {@code save} asigna un id a las
 * preguntas nuevas y {@code findById} devuelve un {@link Optional}. Se usa en la prueba de intercambiabilidad.
 */
public class InMemoryQuestionAdapter implements PreguntaRepositoryPort {

    private final Map<Long, Pregunta> preguntas = new ConcurrentHashMap<>();
    private final AtomicLong secuencia = new AtomicLong();

    @Override
    public Pregunta save(Pregunta pregunta) {
        Objects.requireNonNull(pregunta, "pregunta");
        if (pregunta.getId() == null) {
            pregunta.asignarId(secuencia.incrementAndGet());
        }
        preguntas.put(pregunta.getId(), pregunta);
        return pregunta;
    }

    @Override
    public Optional<Pregunta> findById(Long id) {
        return id == null ? Optional.empty() : Optional.ofNullable(preguntas.get(id));
    }
}
