package donaton.bff;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Backend For Frontend — SmartLogix
 * Puerto: 8080
 *
 * Patrones implementados:
 *  - BFF: agrega datos de MS-Inventario (8081) y MS-Pedidos (8082)
 *    en responses optimizados para el frontend React.
 *  - Circuit Breaker (Resilience4j): protege al BFF de fallos en cascada
 *    cuando alguno de los microservicios no responde o responde lento
 *    (ver paquete donaton.bff.client).
 */
@SpringBootApplication
public class BffDonatonApplication {

    public static void main(String[] args) {
        SpringApplication.run(BffDonatonApplication.class, args);
    }

    /**
     * RestTemplate con timeouts explícitos de conexión y lectura.
     * Es indispensable definir timeouts para que el Circuit Breaker pueda
     * contabilizar como "fallo" una llamada que se cuelga, en vez de
     * esperar indefinidamente la respuesta de un microservicio caído.
     */
    @Bean
    public RestTemplate restTemplate(
            RestTemplateBuilder builder,
            @Value("${bff.http.connect-timeout-ms:2000}") long connectTimeoutMs,
            @Value("${bff.http.read-timeout-ms:3000}") long readTimeoutMs) {
        return builder
                .setConnectTimeout(Duration.ofMillis(connectTimeoutMs))
                .setReadTimeout(Duration.ofMillis(readTimeoutMs))
                .build();
    }
}
