package co.edu.unicauca.saberpro.domain.ports.outbound;

import co.edu.unicauca.saberpro.domain.model.Pregunta;

import java.util.Optional;

/**
 * Puerto de salida (driven) de persistencia del agregado {@link Pregunta}.
 * <p>
 * Interfaz pura: no conoce JPA, SQL ni Spring. La implementan {@code JpaQuestionAdapter} (H2)
 * e {@code InMemoryQuestionAdapter} (HashMap), que se pueden intercambiar sin tocar el dominio.
 */
public interface PreguntaRepositoryPort {

    /**
     * Guarda una pregunta nueva (le asigna identificador) o actualiza una existente.
     *
     * @param pregunta agregado completo
     * @return la pregunta guardada, con identificador
     */
    Pregunta save(Pregunta pregunta);

    /**
     * Busca una pregunta por su identificador.
     *
     * @param id identificador
     * @return la pregunta, si existe
     */
    Optional<Pregunta> findById(Long id);
}
