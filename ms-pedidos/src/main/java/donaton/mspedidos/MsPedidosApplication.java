package donaton.mspedidos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
 *
 * Persistencia: JPA/Hibernate + H2 (archivo ./data/pedidos-db).
 * Documentación API: /swagger-ui.html
 */
@SpringBootApplication
public class MsPedidosApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsPedidosApplication.class, args);
    }
}
