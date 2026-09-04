package donaton.msinventario.repository;

import donaton.msinventario.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * PATRÓN: Repository Pattern
 * ─────────────────────────────────────────────────────────────────────────
 * Extiende Spring Data JPA (JpaRepository), lo que provee persistencia real
 * sobre la base de datos H2 (CRUD, paginación, etc.) sin necesidad de
 * escribir SQL manual. Desacopla completamente la lógica de negocio
 * (InventarioService) de los detalles del motor de persistencia: cambiar de
 * H2 a PostgreSQL/MySQL solo requiere ajustar application.properties.
 */
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByEstado(String estado);

    List<Producto> findByBodegaId(Long bodegaId);
}
