package donaton.bff.service;

import donaton.bff.client.InventarioClient;
import donaton.bff.client.PedidosClient;
import donaton.bff.client.ServicioResultado;
import donaton.bff.dto.DashboardResumenDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * PATRÓN: Backend For Frontend (BFF)
 * ─────────────────────────────────────────────────────────────────────────
 * Actúa como capa de composición entre el frontend React y los microservicios
 * internos. Agrega y transforma datos de MS-Inventario y MS-Pedidos en un
 * único response optimizado para cada vista del cliente.
 *
 * Beneficio: el frontend realiza 1 llamada en lugar de N llamadas a distintos
 * servicios, simplificando la lógica del cliente y reduciendo la latencia
 * percibida por el usuario.
 *
 * La resiliencia ante fallos de los microservicios (patrón Circuit Breaker)
 * se delega a InventarioClient/PedidosClient: este servicio solo decide
 * qué hacer con el resultado (ServicioResultado) — agregar los datos si
 * están disponibles, o registrar una alerta si el circuito degradó la
 * respuesta — sin conocer los detalles de Resilience4j.
 */
@Service
public class BffService {

    private final InventarioClient inventarioClient;
    private final PedidosClient pedidosClient;

    public BffService(InventarioClient inventarioClient, PedidosClient pedidosClient) {
        this.inventarioClient = inventarioClient;
        this.pedidosClient = pedidosClient;
    }

    /**
     * Agrega estadísticas de inventario y pedidos en un único DTO de dashboard.
     * Reduce N llamadas HTTP del frontend a 1 sola petición al BFF.
     *
     * Si algún microservicio no está disponible (circuito abierto o error),
     * el dashboard igual se responde con los datos que sí llegaron, más una
     * alerta explicando qué componente falló (degradación controlada).
     */
    public DashboardResumenDTO obtenerResumenDashboard() {
        DashboardResumenDTO resumen = new DashboardResumenDTO();
        List<String> alertas = new ArrayList<>();

        aplicarInventario(resumen, alertas);
        aplicarCentros(resumen, alertas);
        aplicarPedidos(resumen, alertas);

        resumen.setAlertas(alertas);
        return resumen;
    }

    private void aplicarInventario(DashboardResumenDTO resumen, List<String> alertas) {
        ServicioResultado<List<Map<String, Object>>> resultado = inventarioClient.obtenerProductos();

        if (!resultado.isDisponible()) {
            alertas.add("⚠ " + resultado.getMensajeError());
            return;
        }

        List<Map<String, Object>> productos = resultado.getDatos();
        resumen.setTotalProductos(productos.size());
        resumen.setProductosDisponibles(contarPorEstado(productos, "DISPONIBLE"));
        resumen.setProductosReservados(contarPorEstado(productos, "RESERVADO"));
        resumen.setProductosAgotados(contarPorEstado(productos, "AGOTADO"));
    }

    private void aplicarCentros(DashboardResumenDTO resumen, List<String> alertas) {
        ServicioResultado<List<Map<String, Object>>> resultado = pedidosClient.obtenerCentros();

        if (!resultado.isDisponible()) {
            alertas.add("⚠ " + resultado.getMensajeError());
            return;
        }

        List<Map<String, Object>> centros = resultado.getDatos();
        resumen.setTotalCentrosDistribucion(centros.size());
        resumen.setCentrosActivos(contarPorEstado(centros, "ACTIVO"));
        resumen.setCentrosSaturados(contarPorEstado(centros, "SATURADO"));
        if (resumen.getCentrosSaturados() > 0) {
            alertas.add("🔴 " + resumen.getCentrosSaturados() + " centros de distribución saturados");
        }
    }

    private void aplicarPedidos(DashboardResumenDTO resumen, List<String> alertas) {
        ServicioResultado<List<Map<String, Object>>> resultado = pedidosClient.obtenerPedidos();

        if (!resultado.isDisponible()) {
            alertas.add("⚠ " + resultado.getMensajeError());
            return;
        }

        List<Map<String, Object>> pedidos = resultado.getDatos();
        resumen.setTotalPedidos(pedidos.size());
        resumen.setPedidosPendientes(contarPorEstado(pedidos, "PENDIENTE"));
        resumen.setPedidosEnCamino(contarPorEstado(pedidos, "EN_CAMINO"));
        resumen.setPedidosEntregados(contarPorEstado(pedidos, "ENTREGADO"));
    }

    private long contarPorEstado(List<Map<String, Object>> lista, String estado) {
        return lista.stream()
                .filter(m -> estado.equalsIgnoreCase((String) m.get("estado")))
                .count();
    }
}
