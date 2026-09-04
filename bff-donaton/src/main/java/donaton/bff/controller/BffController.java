package donaton.bff.controller;

import donaton.bff.dto.DashboardResumenDTO;
import donaton.bff.service.BffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador del BFF.
 * Expone endpoints agregados para el frontend React.
 * Cada endpoint compone datos de múltiples microservicios.
 */
@RestController
@RequestMapping("/bff")
@CrossOrigin(origins = "*")
@Tag(name = "BFF", description = "Backend For Frontend: agrega datos de MS-Inventario y MS-Pedidos")
public class BffController {

    private final BffService bffService;

    public BffController(BffService bffService) {
        this.bffService = bffService;
    }

    /**
     * Dashboard principal: agrega estadísticas de inventario, centros de
     * distribución y pedidos. El frontend React realiza UNA sola llamada
     * en lugar de tres.
     *
     * GET /bff/dashboard
     */
    @Operation(summary = "Obtener resumen agregado del dashboard (inventario + centros + pedidos)")
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResumenDTO> getDashboard() {
        return ResponseEntity.ok(bffService.obtenerResumenDashboard());
    }

    @Operation(summary = "Verificar el estado del BFF")
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("BFF-Donaton OK");
    }
}
