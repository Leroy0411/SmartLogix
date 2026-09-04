package donaton.msinventario;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Microservicio MS-Inventario — SmartLogix
 * Puerto: 8081
 *
 * Cubre el módulo "Gestión de Inventario" del caso: mantiene actualizados
 * los niveles de stock por bodega en tiempo real.
 *
 * Patrones implementados:
 *  - Repository Pattern: ProductoRepository (Spring Data JPA) sobre base H2
 *  - Factory Method: ProductoFactory y subclases concretas por categoría
 *
 * Persistencia: JPA/Hibernate + H2 (archivo ./data/inventario-db).
 * Documentación API: /swagger-ui.html
 */
@SpringBootApplication
public class MsInventarioApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsInventarioApplication.class, args);
    }
}
