package donaton.mspedidos;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Microservicio MS-Pedidos — SmartLogix
 * Puerto: 8082
 *
 * Cubre los módulos "Procesamiento de Pedidos" y "Coordinación de Envíos"
 * del caso: valida, aprueba y asigna pedidos a centros de distribución,
 * y coordina su despacho y entrega con trazabilidad completa.
 *
 * Patrones implementados:
 *  - Repository Pattern: CentroDistribucionRepository / PedidoRepository
 *    (Spring Data JPA) sobre base H2
 *  - Observer: notifica cambios de estado de pedidos a múltiples receptores
 *    (auditoría, notificaciones al transportista/cliente)
 *  - Circuit Breaker (Resilience4j): protege la llamada a MS-Inventario al
 *    despachar un pedido (ver paquete donaton.mspedidos.client)
 *
 * Persistencia: JPA/Hibernate + H2 (archivo ./data/pedidos-db).
 * Documentación API: /swagger-ui.html
 */
@SpringBootApplication
public class MsPedidosApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsPedidosApplication.class, args);
    }

    /**
     * RestTemplate con timeouts explícitos, igual que en el BFF: sin esto
     * una llamada colgada a MS-Inventario nunca contaría como "fallo" para
     * el Circuit Breaker.
     */
    @Bean
    public RestTemplate restTemplate(
            RestTemplateBuilder builder,
            @Value("${pedidos.http.connect-timeout-ms:2000}") long connectTimeoutMs,
            @Value("${pedidos.http.read-timeout-ms:3000}") long readTimeoutMs) {
        return builder
                .setConnectTimeout(Duration.ofMillis(connectTimeoutMs))
                .setReadTimeout(Duration.ofMillis(readTimeoutMs))
                .build();
    }
}
