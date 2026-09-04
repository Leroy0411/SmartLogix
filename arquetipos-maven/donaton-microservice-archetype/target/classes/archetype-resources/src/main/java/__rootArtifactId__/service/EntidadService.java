package ${package}.service;

import ${package}.model.Entidad;
import ${package}.repository.EntidadRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de negocio base.
 * Usa el Repository Pattern para acceder a datos sin acoplar la lógica
 * de negocio a la implementación de persistencia.
 */
@Service
public class EntidadService {

    private final EntidadRepository repository;

    public EntidadService(EntidadRepository repository) {
        this.repository = repository;
    }

    public List<Entidad> obtenerTodas() {
        return repository.findAll();
    }

    public Optional<Entidad> obtenerPorId(Long id) {
        return repository.findById(id);
    }

    public List<Entidad> obtenerPorEstado(String estado) {
        return repository.findByEstado(estado);
    }

    public Entidad crear(Entidad entidad) {
        if (entidad.getEstado() == null || entidad.getEstado().isBlank()) {
            entidad.setEstado("ACTIVO");
        }
        return repository.save(entidad);
    }

    public Optional<Entidad> actualizarEstado(Long id, String nuevoEstado) {
        return repository.findById(id).map(e -> {
            e.setEstado(nuevoEstado.toUpperCase());
            return repository.save(e);
        });
    }

    public boolean eliminar(Long id) {
        return repository.deleteById(id);
    }
}
