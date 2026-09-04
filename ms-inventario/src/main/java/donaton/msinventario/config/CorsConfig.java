package donaton.msinventario.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración centralizada de CORS.
 *
 * Reemplaza el {@code @CrossOrigin(origins = "*")} que había quedado en el
 * controlador: aceptar cualquier origen es aceptable para una demo rápida,
 * pero no es una postura defendible en el informe (apartado de seguridad).
 * El origen permitido ahora es configurable vía {@code app.cors.allowed-origin}
 * (por defecto, el frontend en desarrollo: http://localhost:5173).
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origin:http://localhost:5173}")
    private String allowedOrigin;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigin)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
