package ec.edu.espe.ventanilla.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import java.time.Duration;

/**
 * Configuración de RestTemplate para comunicación con servicios externos
 */
@Configuration
public class RestTemplateConfig {

    /**
     * Bean de RestTemplate con timeouts configurados
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .connectTimeout(Duration.ofSeconds(10))
                .readTimeout(Duration.ofSeconds(30))
                .build();
    }
}