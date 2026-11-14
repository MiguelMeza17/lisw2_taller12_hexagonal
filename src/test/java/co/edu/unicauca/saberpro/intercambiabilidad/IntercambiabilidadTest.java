package co.edu.unicauca.saberpro.intercambiabilidad;

import co.edu.unicauca.saberpro.domain.ports.outbound.PreguntaRepositoryPort;
import co.edu.unicauca.saberpro.infrastructure.adapter.out.persistence.jpa.JpaQuestionAdapter;
import co.edu.unicauca.saberpro.infrastructure.adapter.out.persistence.memory.InMemoryQuestionAdapter;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba de intercambiabilidad (sección 4 del taller): el mismo flujo REST, con los mismos asertos,
 * se ejecuta una vez con {@link JpaQuestionAdapter} y otra con {@link InMemoryQuestionAdapter}.
 * Lo único que cambia entre ambos casos es la propiedad {@code saberpro.persistencia}; el paquete
 * {@code domain} y el servicio de aplicación son los mismos.
 */
class IntercambiabilidadTest {

    private static final String PREGUNTA = """
            {"enunciado": "¿Qué patrón aísla el dominio de la infraestructura?",
             "justificacion": "Los puertos y adaptadores aíslan el núcleo.",
             "opciones": [{"texto": "Hexagonal", "esCorrecta": true},
                          {"texto": "Singleton", "esCorrecta": false},
                          {"texto": "Monolito", "esCorrecta": false},
                          {"texto": "Capas acopladas", "esCorrecta": false}]}
            """;

    /** Flujo común: crear (201) → enviar a revisión (200) → consultar (200) → invariante incumplida (400). */
    abstract static class FlujoComun {

        @Autowired
        MockMvc mvc;

        @Autowired
        PreguntaRepositoryPort repositorio;

        @Test
        void elFlujoCompletoFuncionaIgual() throws Exception {
            String creada = mvc.perform(post("/api/preguntas").contentType(MediaType.APPLICATION_JSON).content(PREGUNTA))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.estado").value("BORRADOR"))
                    .andExpect(jsonPath("$.opciones", hasSize(4)))
                    .andReturn().getResponse().getContentAsString();
            long id = ((Number) JsonPath.read(creada, "$.id")).longValue();

            mvc.perform(post("/api/preguntas/{id}/enviar-revision", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.estado").value("PENDIENTE_REVISION"));

            mvc.perform(get("/api/preguntas/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id))
                    .andExpect(jsonPath("$.estado").value("PENDIENTE_REVISION"));

            mvc.perform(post("/api/preguntas").contentType(MediaType.APPLICATION_JSON)
                            .content(PREGUNTA.replace("Los puertos y adaptadores aíslan el núcleo.", "corta")))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @SpringBootTest(properties = "saberpro.persistencia=jpa")
    @AutoConfigureMockMvc
    class ConAdaptadorJpa extends FlujoComun {

        @Test
        void elPuertoUsaJpa() {
            assertInstanceOf(JpaQuestionAdapter.class, repositorio);
        }
    }

    @Nested
    @SpringBootTest(properties = "saberpro.persistencia=memoria")
    @AutoConfigureMockMvc
    class ConAdaptadorEnMemoria extends FlujoComun {

        @Test
        void elPuertoUsaMemoria() {
            assertInstanceOf(InMemoryQuestionAdapter.class, repositorio);
        }
    }
}
