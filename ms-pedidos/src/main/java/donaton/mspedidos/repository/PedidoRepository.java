package donaton.mspedidos.repository;

import donaton.mspedidos.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * PATRÓN: Repository Pattern — Pedidos
 * Extiende Spring Data JPA, proveyendo persistencia real sobre H2.
 */
@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByEstado(String estado);
}
