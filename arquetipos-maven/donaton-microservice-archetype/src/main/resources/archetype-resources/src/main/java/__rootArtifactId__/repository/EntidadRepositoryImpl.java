package ${package}.repository;

import ${package}.model.Entidad;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Implementación en memoria del repositorio.
 * Reemplazar con JpaRepository en producción sin modificar el servicio.
 */
@Repository
public class EntidadRepositoryImpl implements EntidadRepository {

    private final List<Entidad> almacen = new ArrayList<>();
    private final AtomicLong secuencia  = new AtomicLong(1);

    @Override
    public List<Entidad> findAll() { return new ArrayList<>(almacen); }

    @Override
    public Optional<Entidad> findById(Long id) {
        return almacen.stream().filter(e -> e.getId().equals(id)).findFirst();
    }

    @Override
    public List<Entidad> findByEstado(String estado) {
        List<Entidad> res = new ArrayList<>();
        for (Entidad e : almacen) {
            if (e.getEstado().equalsIgnoreCase(estado)) res.add(e);
        }
        return res;
    }

    @Override
    public Entidad save(Entidad entidad) {
        if (entidad.getId() == null) {
            entidad.setId(secuencia.getAndIncrement());
            almacen.add(entidad);
        } else {
            for (int i = 0; i < almacen.size(); i++) {
                if (almacen.get(i).getId().equals(entidad.getId())) {
                    almacen.set(i, entidad);
                    return entidad;
                }
            }
        }
        return entidad;
    }

    @Override
    public boolean deleteById(Long id) {
        return almacen.removeIf(e -> e.getId().equals(id));
    }
}
