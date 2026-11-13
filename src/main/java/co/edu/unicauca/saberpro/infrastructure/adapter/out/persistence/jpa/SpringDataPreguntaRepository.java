package co.edu.unicauca.saberpro.infrastructure.adapter.out.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de Spring Data sobre la tabla de preguntas. Solo lo usa {@link JpaQuestionAdapter}.
 */
public interface SpringDataPreguntaRepository extends JpaRepository<PreguntaJpaEntity, Long> {
}
