package ${package}.controller;

import ${package}.model.Entidad;
import ${package}.service.EntidadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST base generado por el arquetipo Donaton.
 * Adaptar la ruta @RequestMapping al dominio del microservicio.
 */
@RestController
@RequestMapping("/api/${rootArtifactId}")
@CrossOrigin(origins = "*")
public class EntidadController {

    private final EntidadService service;

    public EntidadController(EntidadService service) {
        this.service = service;
    }

    @GetMapping
    public List<Entidad> listar() { return service.obtenerTodas(); }

    @GetMapping("/{id}")
    public ResponseEntity<Entidad> obtener(@PathVariable Long id) {
        return service.obtenerPorId(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Entidad> crear(@RequestBody Entidad entidad) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(entidad));
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<?> actualizarEstado(@PathVariable Long id,
                                               @RequestParam String estado) {
        return service.actualizarEstado(id, estado)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> eliminar(@PathVariable Long id) {
        if (service.eliminar(id)) {
            return ResponseEntity.ok(Map.of("mensaje", "Eliminado correctamente"));
        }
        return ResponseEntity.notFound().build();
    }
}
