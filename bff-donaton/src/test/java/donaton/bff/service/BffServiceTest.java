package donaton.bff.service;

import donaton.bff.client.InventarioClient;
import donaton.bff.client.PedidosClient;
import donaton.bff.client.ServicioResultado;
import donaton.bff.dto.DashboardResumenDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para BffService.
 * Valida que el BFF agrega correctamente los datos que devuelven los
 * clientes protegidos por Circuit Breaker (InventarioClient/PedidosClient)
 * y que se degrada de forma controlada cuando un ServicioResultado llega
 * como "no disponible" (es decir, cuando el circuito correspondiente
 * cayó a fallback).
 */
class BffServiceTest {

    @Mock
    private InventarioClient inventarioClient;

    @Mock
    private PedidosClient pedidosClient;

    private BffService bffService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        bffService = new BffService(inventarioClient, pedidosClient);

        List<Map<String, Object>> productos = List.of(
                Map.of("id", 1, "categoria", "ELECTRONICA", "estado", "DISPONIBLE"),
                Map.of("id", 2, "categoria", "HOGAR", "estado", "RESERVADO"),
                Map.of("id", 3, "categoria", "ALIMENTOS", "estado", "AGOTADO")
        );

        List<Map<String, Object>> centros = List.of(
                Map.of("id", 1, "nombre", "Centro A", "estado", "ACTIVO"),
                Map.of("id", 2, "nombre", "Centro B", "estado", "SATURADO")
        );

        List<Map<String, Object>> pedidos = List.of(
                Map.of("id", 1, "estado", "PENDIENTE"),
                Map.of("id", 2, "estado", "EN_CAMINO"),
                Map.of("id", 3, "estado", "ENTREGADO")
        );

        when(inventarioClient.obtenerProductos()).thenReturn(ServicioResultado.ok(productos));
        when(pedidosClient.obtenerCentros()).thenReturn(ServicioResultado.ok(centros));
        when(pedidosClient.obtenerPedidos()).thenReturn(ServicioResultado.ok(pedidos));
    }

    @Test
    @DisplayName("Dashboard debe agregar correctamente estadísticas de inventario")
    void dashboard_agregaInventarioCorrectamente() {
        DashboardResumenDTO resultado = bffService.obtenerResumenDashboard();

        assertEquals(3, resultado.getTotalProductos());
        assertEquals(1, resultado.getProductosDisponibles());
        assertEquals(1, resultado.getProductosReservados());
        assertEquals(1, resultado.getProductosAgotados());
    }

    @Test
    @DisplayName("Dashboard debe agregar correctamente estadísticas de centros")
    void dashboard_agregaCentrosCorrectamente() {
        DashboardResumenDTO resultado = bffService.obtenerResumenDashboard();

        assertEquals(2, resultado.getTotalCentrosDistribucion());
        assertEquals(1, resultado.getCentrosActivos());
        assertEquals(1, resultado.getCentrosSaturados());
    }

    @Test
    @DisplayName("Dashboard debe agregar correctamente estadísticas de pedidos")
    void dashboard_agregaPedidosCorrectamente() {
        DashboardResumenDTO resultado = bffService.obtenerResumenDashboard();

        assertEquals(3, resultado.getTotalPedidos());
        assertEquals(1, resultado.getPedidosPendientes());
        assertEquals(1, resultado.getPedidosEnCamino());
        assertEquals(1, resultado.getPedidosEntregados());
    }

    @Test
    @DisplayName("Dashboard debe incluir alerta cuando hay centros saturados")
    void dashboard_centroSaturado_generaAlerta() {
        DashboardResumenDTO resultado = bffService.obtenerResumenDashboard();

        assertFalse(resultado.getAlertas().isEmpty());
        assertTrue(resultado.getAlertas().stream()
                .anyMatch(a -> a.contains("saturado")));
    }

    @Test
    @DisplayName("Si el circuito de MS-Inventario cae a fallback, el BFF registra la alerta y continúa")
    void dashboard_circuitoInventarioAbierto_registraAlertaYContinua() {
        when(inventarioClient.obtenerProductos())
                .thenReturn(ServicioResultado.fallo(List.of(), "MS-Inventario no disponible: Connection refused"));

        DashboardResumenDTO resultado = bffService.obtenerResumenDashboard();

        // No debe lanzar excepción
        assertNotNull(resultado);
        assertEquals(0, resultado.getTotalProductos());
        // Debe registrar alerta de indisponibilidad
        assertTrue(resultado.getAlertas().stream()
                .anyMatch(a -> a.contains("MS-Inventario")));
        // Las estadísticas de pedidos aún deben estar disponibles
        assertEquals(2, resultado.getTotalCentrosDistribucion());
        assertEquals(3, resultado.getTotalPedidos());
    }

    @Test
    @DisplayName("Si el circuito de MS-Pedidos cae a fallback, el BFF sigue mostrando inventario")
    void dashboard_circuitoPedidosAbierto_registraAlertaYContinua() {
        when(pedidosClient.obtenerCentros())
                .thenReturn(ServicioResultado.fallo(List.of(), "MS-Pedidos (centros de distribución) no disponible: timeout"));
        when(pedidosClient.obtenerPedidos())
                .thenReturn(ServicioResultado.fallo(List.of(), "MS-Pedidos (pedidos) no disponible: timeout"));

        DashboardResumenDTO resultado = bffService.obtenerResumenDashboard();

        assertNotNull(resultado);
        assertEquals(3, resultado.getTotalProductos());
        assertEquals(0, resultado.getTotalCentrosDistribucion());
        assertEquals(0, resultado.getTotalPedidos());
        assertEquals(2, resultado.getAlertas().size());
        assertTrue(resultado.getAlertas().stream().anyMatch(a -> a.contains("centros de distribución")));
        assertTrue(resultado.getAlertas().stream().anyMatch(a -> a.contains("pedidos")));
    }
}
