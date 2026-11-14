package co.edu.unicauca.saberpro.infrastructure.config;

import co.edu.unicauca.saberpro.domain.model.EstadoPregunta;
import co.edu.unicauca.saberpro.domain.model.Pregunta;
import co.edu.unicauca.saberpro.domain.ports.inbound.ConsultarPreguntaUseCase;
import co.edu.unicauca.saberpro.domain.ports.inbound.CrearPreguntaComando;
import co.edu.unicauca.saberpro.domain.ports.inbound.CrearPreguntaComando.Opcion;
import co.edu.unicauca.saberpro.domain.ports.inbound.CrearPreguntaUseCase;
import co.edu.unicauca.saberpro.domain.ports.outbound.PreguntaRepositoryPort;
import co.edu.unicauca.saberpro.infrastructure.adapter.out.persistence.jpa.JpaQuestionAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Verifica el cableado por defecto: los puertos de entrada quedan conectados al adaptador JPA.
 */
@SpringBootTest
class HexagonalConfigurationTest {

    @Autowired
    private PreguntaRepositoryPort repositorio;

    @Autowired
    private CrearPreguntaUseCase crearPregunta;

    @Autowired
    private ConsultarPreguntaUseCase consultarPregunta;

    @Test
    void porDefectoSeUsaElAdaptadorJpa() {
        assertInstanceOf(JpaQuestionAdapter.class, repositorio);
    }

    @Test
    void losCasosDeUsoFuncionanDeExtremoAExtremoConJpa() {
        CrearPreguntaComando comando = new CrearPreguntaComando("¿Qué patrón aísla el dominio?",
                "Los puertos y adaptadores aíslan el núcleo.",
                List.of(new Opcion("Hexagonal", true), new Opcion("Singleton", false),
                        new Opcion("Monolito", false), new Opcion("Capas acopladas", false)));

        Long id = crearPregunta.crear(comando).getId();
        crearPregunta.enviarARevision(id);

        Pregunta leida = consultarPregunta.obtener(id);
        assertEquals(EstadoPregunta.PENDIENTE_REVISION, leida.getEstado());
    }
}
