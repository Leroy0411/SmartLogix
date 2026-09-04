package donaton.msinventario.controller;

import donaton.msinventario.dto.ProductoCreateRequest;
import donaton.msinventario.model.Producto;
import donaton.msinventario.service.InventarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST de MS-Inventario.
 * Expone las operaciones del módulo "Gestión de Inventario" de SmartLogix.
 *
 * CORS ya no acepta "*": ver {@link donaton.msinventario.config.CorsConfig}.
 * Las escrituras (POST/PUT/DELETE) exigen header X-API-KEY:
 * ver {@link donaton.msinventario.security.ApiKeyFilter}.
 */
@RestController
@RequestMapping("/api/inventario")
@Tag(name = "Inventario", description = "Gestión de stock de productos por bodega (con Factory Method)")
public class InventarioController {

    private final InventarioService inventarioService;

    public InventarioController(InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    @Operation(summary = "Listar todos los productos en inventario")
    @GetMapping
    public List<Producto> listar() {
        return inventarioService.obtenerTodos();
    }

    @Operation(summary = "Obtener un producto por su ID")
    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerPorId(@PathVariable Long id) {
        return inventarioService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Listar productos filtrados por estado (DISPONIBLE, RESERVADO, AGOTADO)")
    @GetMapping("/estado/{estado}")
    public List<Producto> listarPorEstado(@PathVariable String estado) {
        return inventarioService.obtenerPorEstado(estado);
    }

    @Operation(summary = "Listar productos de una bodega específica")
    @GetMapping("/bodega/{bodegaId}")
    public List<Producto> listarPorBodega(@PathVariable Long bodegaId) {
        return inventarioService.obtenerPorBodega(bodegaId);
    }

    @Operation(summary = "Registrar un nuevo producto en inventario (aplica Factory Method según categoría)")
    @PostMapping
    public ResponseEntity<Producto> crear(@Valid @RequestBody ProductoCreateRequest request) {
        Producto creado = inventarioService.crearProducto(
                request.getCategoria(),
                request.getProveedor(),
                request.getStock(),
                request.getBodegaId(),
                request.getDescripcion()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @Operation(summary = "Actualizar el estado de un producto")
    @PutMapping("/{id}/estado")
    public ResponseEntity<?> actualizarEstado(@PathVariable Long id, @RequestParam String estado) {
        return inventarioService.actualizarEstado(id, estado)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Descontar stock de un producto (por ejemplo, al confirmarse un pedido)")
    @PutMapping("/{id}/descontar-stock")
    public ResponseEntity<?> descontarStock(@PathVariable Long id, @RequestParam Integer cantidad) {
        return inventarioService.descontarStock(id, cantidad)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Eliminar un producto del inventario")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> eliminar(@PathVariable Long id) {
        if (inventarioService.eliminar(id)) {
            return ResponseEntity.ok(Map.of("mensaje", "Producto eliminado"));
        }
        return ResponseEntity.notFound().build();
    }
}
