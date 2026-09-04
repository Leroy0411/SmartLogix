package donaton.mspedidos.controller;

import donaton.mspedidos.model.CentroDistribucion;
import donaton.mspedidos.model.Pedido;
import donaton.mspedidos.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST de MS-Pedidos.
 * Expone las operaciones de los módulos "Procesamiento de Pedidos" y
 * "Coordinación de Envíos" de SmartLogix.
 */
@RestController
@RequestMapping("/api/pedidos")
@CrossOrigin(origins = "*")
@Tag(name = "Pedidos", description = "Procesamiento de pedidos y coordinación de envíos (con patrón Observer)")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    // ── Centros de Distribución ─────────────────────────────────────────

    @Operation(summary = "Listar todos los centros de distribución")
    @GetMapping("/centros-distribucion")
    public List<CentroDistribucion> listarCentros() {
        return pedidoService.obtenerCentros();
    }

    @Operation(summary = "Listar centros de distribución en estado ACTIVO")
    @GetMapping("/centros-distribucion/activos")
    public List<CentroDistribucion> listarActivos() {
        return pedidoService.obtenerCentrosActivos();
    }

    @Operation(summary = "Listar centros activos que aún tienen capacidad disponible")
    @GetMapping("/centros-distribucion/con-capacidad")
    public List<CentroDistribucion> listarConCapacidad() {
        return pedidoService.obtenerCentrosConCapacidad();
    }

    @Operation(summary = "Registrar un nuevo centro de distribución")
    @PostMapping("/centros-distribucion")
    public ResponseEntity<CentroDistribucion> crearCentro(@RequestBody CentroDistribucion centro) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.agregarCentro(centro));
    }

    @Operation(summary = "Actualizar la ocupación actual de un centro de distribución")
    @PutMapping("/centros-distribucion/{id}/ocupacion")
    public ResponseEntity<?> actualizarOcupacion(@PathVariable Long id,
                                                  @RequestParam Integer ocupacion) {
        return pedidoService.actualizarOcupacion(id, ocupacion)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Eliminar un centro de distribución por su ID")
    @DeleteMapping("/centros-distribucion/{id}")
    public ResponseEntity<Map<String, String>> eliminarCentro(@PathVariable Long id) {
        if (pedidoService.eliminarCentro(id)) {
            return ResponseEntity.ok(Map.of("mensaje", "Centro eliminado"));
        }
        return ResponseEntity.notFound().build();
    }

    // ── Pedidos (con Observer) ────────────────────────────────────────

    @Operation(summary = "Listar todos los pedidos")
    @GetMapping
    public List<Pedido> listarPedidos() {
        return pedidoService.obtenerPedidos();
    }

    @Operation(summary = "Listar pedidos filtrados por estado")
    @GetMapping("/estado/{estado}")
    public List<Pedido> listarPorEstado(@PathVariable String estado) {
        return pedidoService.obtenerPorEstado(estado);
    }

    @Operation(summary = "Crear un nuevo pedido (notifica a los observadores registrados)")
    @PostMapping
    public ResponseEntity<Pedido> crearPedido(@RequestBody Pedido pedido) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.crearPedido(pedido));
    }

    @Operation(summary = "Marcar un pedido como despachado (EN_CAMINO)")
    @PutMapping("/{id}/despachar")
    public ResponseEntity<?> despachar(@PathVariable Long id) {
        return pedidoService.despacharPedido(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Confirmar la entrega de un pedido")
    @PutMapping("/{id}/entregar")
    public ResponseEntity<?> entregar(@PathVariable Long id,
                                       @RequestParam(defaultValue = "") String observaciones) {
        return pedidoService.confirmarEntrega(id, observaciones)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Cancelar un pedido")
    @PutMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelar(@PathVariable Long id,
                                       @RequestParam(defaultValue = "") String motivo) {
        return pedidoService.cancelarPedido(id, motivo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Eliminar un pedido por su ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> eliminarPedido(@PathVariable Long id) {
        if (pedidoService.eliminarPedido(id)) {
            return ResponseEntity.ok(Map.of("mensaje", "Pedido eliminado"));
        }
        return ResponseEntity.notFound().build();
    }
}
