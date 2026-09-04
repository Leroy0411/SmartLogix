package ${package}.repository;

import ${package}.model.Entidad;

import java.util.List;
import java.util.Optional;

/**
 * PATRÓN: Repository Pattern
 * Interfaz de acceso a datos — implementar con JPA en producción.
 */
public interface EntidadRepository {
    List<Entidad> findAll();
    Optional<Entidad> findById(Long id);
    List<Entidad> findByEstado(String estado);
    Entidad save(Entidad entidad);
    boolean deleteById(Long id);
}
