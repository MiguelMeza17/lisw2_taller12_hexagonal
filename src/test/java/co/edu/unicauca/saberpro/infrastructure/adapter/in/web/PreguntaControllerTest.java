package co.edu.unicauca.saberpro.infrastructure.adapter.in.web;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PreguntaControllerTest {

    static final String VALIDA = """
            {"enunciado": "¿Qué patrón aísla el dominio de la infraestructura?",
             "justificacion": "Los puertos y adaptadores aíslan el núcleo.",
             "opciones": [{"texto": "Hexagonal", "esCorrecta": true},
                          {"texto": "Singleton", "esCorrecta": false},
                          {"texto": "Monolito", "esCorrecta": false},
                          {"texto": "Capas acopladas", "esCorrecta": false}]}
            """;

    @Autowired
    private MockMvc mvc;

    private long crear(String json) throws Exception {
        String body = mvc.perform(post("/api/preguntas").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    @Test
    void crearResponde201EnBorrador() throws Exception {
        mvc.perform(post("/api/preguntas").contentType(MediaType.APPLICATION_JSON).content(VALIDA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.estado").value("BORRADOR"))
                .andExpect(jsonPath("$.opciones", hasSize(4)));
    }

    @Test
    void enviarARevisionPasaAPendiente() throws Exception {
        long id = crear(VALIDA);

        mvc.perform(post("/api/preguntas/{id}/enviar-revision", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PENDIENTE_REVISION"));
        mvc.perform(get("/api/preguntas/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PENDIENTE_REVISION"));
    }

    @Test
    void justificacionCortaResponde400() throws Exception {
        String json = VALIDA.replace("Los puertos y adaptadores aíslan el núcleo.", "corta");

        mvc.perform(post("/api/preguntas").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("La justificación debe contener al menos 15 caracteres."));
    }

    @Test
    void enviarSinCuatroOpcionesResponde400() throws Exception {
        String json = """
                {"enunciado": "Enunciado de prueba", "justificacion": "Justificación suficiente.",
                 "opciones": [{"texto": "A", "esCorrecta": true}, {"texto": "B", "esCorrecta": false}]}
                """;
        long id = crear(json);

        mvc.perform(post("/api/preguntas/{id}/enviar-revision", id))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Debe tener exactamente 4 opciones de respuesta."));
    }

    @Test
    void preguntaInexistenteResponde404() throws Exception {
        mvc.perform(get("/api/preguntas/{id}", 9999))
                .andExpect(status().isNotFound());
    }
}
