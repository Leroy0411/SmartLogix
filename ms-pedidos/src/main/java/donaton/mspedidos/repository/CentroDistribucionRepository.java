package donaton.mspedidos.repository;

import donaton.mspedidos.model.CentroDistribucion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * PATRÓN: Repository Pattern — Centros de Distribución
 * Extiende Spring Data JPA, proveyendo persistencia real sobre H2.
 */
@Repository
public interface CentroDistribucionRepository extends JpaRepository<CentroDistribucion, Long> {
    List<CentroDistribucion> findByEstado(String estado);
}
