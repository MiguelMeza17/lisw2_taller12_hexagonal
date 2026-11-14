package co.edu.unicauca.saberpro.infrastructure.config;

import co.edu.unicauca.saberpro.application.service.CrearPreguntaService;
import co.edu.unicauca.saberpro.domain.ports.outbound.NotificacionPort;
import co.edu.unicauca.saberpro.domain.ports.outbound.PreguntaRepositoryPort;
import co.edu.unicauca.saberpro.infrastructure.adapter.out.persistence.jpa.JpaQuestionAdapter;
import co.edu.unicauca.saberpro.infrastructure.adapter.out.persistence.jpa.SpringDataPreguntaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Conecta los adaptadores con los puertos del hexágono. Es el único lugar que se toca para cambiar
 * de tecnología: el paquete {@code domain} no cambia.
 * <p>
 * El adaptador de persistencia se elige con la propiedad {@value #PROPIEDAD} en
 * {@code application.properties} (o con {@code --saberpro.persistencia=memoria} al arrancar):
 * <ul>
 *     <li>{@code jpa} (por defecto): {@link JpaQuestionAdapter} sobre H2.</li>
 *     <li>{@code memoria}: {@code InMemoryQuestionAdapter} sobre HashMap (lo agrega el issue #8 en este
 *     mismo archivo, con {@code @ConditionalOnProperty(name = PROPIEDAD, havingValue = "memoria")}).</li>
 * </ul>
 */
@Configuration
public class HexagonalConfiguration {

    /** Propiedad que decide el adaptador de persistencia. */
    public static final String PROPIEDAD = "saberpro.persistencia";

    private static final Logger LOG = LoggerFactory.getLogger(HexagonalConfiguration.class);

    /**
     * Adaptador JPA, activo cuando {@value #PROPIEDAD} vale {@code jpa} o no está definida.
     */
    @Bean
    @ConditionalOnProperty(name = PROPIEDAD, havingValue = "jpa", matchIfMissing = true)
    public PreguntaRepositoryPort jpaQuestionAdapter(SpringDataPreguntaRepository repositorio) {
        LOG.info("Adaptador de persistencia activo: JpaQuestionAdapter (H2)");
        return new JpaQuestionAdapter(repositorio);
    }

    /**
     * Servicio de aplicación que implementa los puertos de entrada. Recibe el adaptador de persistencia
     * activo sin saber cuál es. Si todavía no hay adaptador de notificaciones registrado, usa uno que
     * solo deja constancia en el log.
     */
    @Bean
    public CrearPreguntaService crearPreguntaService(PreguntaRepositoryPort repositorio,
                                                     ObjectProvider<NotificacionPort> notificaciones) {
        NotificacionPort notificacion = notificaciones.getIfAvailable(() -> pregunta ->
                LOG.info("Pregunta {} enviada a revisión (sin adaptador de notificaciones registrado)", pregunta.getId()));
        return new CrearPreguntaService(repositorio, notificacion);
    }
}
