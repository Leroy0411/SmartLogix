package donaton.bff.dto;

import java.util.List;

/**
 * DTO de resumen del dashboard principal.
 *
 * PATRÓN: Backend For Frontend (BFF)
 * ─────────────────────────────────────────────────────────────────────────
 * Agrega datos de MS-Inventario y MS-Pedidos en un único payload
 * optimizado para la vista principal del frontend React, reduciendo el
 * número de llamadas HTTP desde el cliente de N a 1.
 */
public class DashboardResumenDTO {

    private long totalProductos;
    private long productosDisponibles;
    private long productosReservados;
    private long productosAgotados;

    private long totalCentrosDistribucion;
    private long centrosActivos;
    private long centrosSaturados;

    private long totalPedidos;
    private long pedidosPendientes;
    private long pedidosEnCamino;
    private long pedidosEntregados;

    private List<String> alertas;

    // Getters y Setters
    public long getTotalProductos() { return totalProductos; }
    public void setTotalProductos(long totalProductos) { this.totalProductos = totalProductos; }
    public long getProductosDisponibles() { return productosDisponibles; }
    public void setProductosDisponibles(long productosDisponibles) { this.productosDisponibles = productosDisponibles; }
    public long getProductosReservados() { return productosReservados; }
    public void setProductosReservados(long productosReservados) { this.productosReservados = productosReservados; }
    public long getProductosAgotados() { return productosAgotados; }
    public void setProductosAgotados(long productosAgotados) { this.productosAgotados = productosAgotados; }
    public long getTotalCentrosDistribucion() { return totalCentrosDistribucion; }
    public void setTotalCentrosDistribucion(long totalCentrosDistribucion) { this.totalCentrosDistribucion = totalCentrosDistribucion; }
    public long getCentrosActivos() { return centrosActivos; }
    public void setCentrosActivos(long centrosActivos) { this.centrosActivos = centrosActivos; }
    public long getCentrosSaturados() { return centrosSaturados; }
    public void setCentrosSaturados(long centrosSaturados) { this.centrosSaturados = centrosSaturados; }
    public long getTotalPedidos() { return totalPedidos; }
    public void setTotalPedidos(long totalPedidos) { this.totalPedidos = totalPedidos; }
    public long getPedidosPendientes() { return pedidosPendientes; }
    public void setPedidosPendientes(long pedidosPendientes) { this.pedidosPendientes = pedidosPendientes; }
    public long getPedidosEnCamino() { return pedidosEnCamino; }
    public void setPedidosEnCamino(long pedidosEnCamino) { this.pedidosEnCamino = pedidosEnCamino; }
    public long getPedidosEntregados() { return pedidosEntregados; }
    public void setPedidosEntregados(long pedidosEntregados) { this.pedidosEntregados = pedidosEntregados; }
    public List<String> getAlertas() { return alertas; }
    public void setAlertas(List<String> alertas) { this.alertas = alertas; }
}
