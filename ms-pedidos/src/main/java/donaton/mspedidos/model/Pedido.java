package donaton.mspedidos.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Entidad JPA que representa un pedido en la plataforma SmartLogix.
 * Unifica el "Procesamiento de Pedidos" y la "Coordinación de Envíos" del
 * caso: un pedido se valida y asigna a un centro de distribución, y luego
 * su mismo ciclo de vida refleja el estado de despacho y entrega.
 * Persistida en base de datos H2 mediante Spring Data JPA (Hibernate).
 */
@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El centro de distribución es obligatorio")
    @Column(name = "centro_distribucion_id", nullable = false)
    private Long centroDistribucionId;

    @Column(name = "cliente_id")
    private Long clienteId;

    @NotBlank(message = "La dirección de entrega es obligatoria")
    @Column(name = "direccion_entrega", nullable = false)
    private String direccionEntrega;

    @Column(name = "responsable_transporte")
    private String responsableTransporte;

    @Column(name = "patente_vehiculo")
    private String patenteVehiculo;

    /**
     * Producto de MS-Inventario asociado a este pedido, y la cantidad
     * despachada. Es opcional (pedidos históricos o de solo-servicio pueden
     * no estar ligados a un ítem de inventario), pero cuando está presente,
     * al despachar el pedido MS-Pedidos descuenta este stock en
     * MS-Inventario (ver PedidoService.despacharPedido / InventarioClient).
     */
    @Column(name = "producto_id")
    private Long productoId;

    @Min(value = 1, message = "La cantidad del producto debe ser mayor que cero")
    @Column(name = "cantidad_producto")
    private Integer cantidadProducto;

    @Column(nullable = false, length = 20)
    private String estado;              // PENDIENTE, EN_CAMINO, ENTREGADO, CANCELADO

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_despacho")
    private LocalDateTime fechaDespacho;

    @Column(name = "fecha_entrega")
    private LocalDateTime fechaEntrega;

    @Column(length = 500)
    private String observaciones;

    public Pedido() {}

    public Pedido(Long id, Long centroDistribucionId, Long clienteId, String direccionEntrega,
                 String responsableTransporte, String patenteVehiculo) {
        this.id = id;
        this.centroDistribucionId = centroDistribucionId;
        this.clienteId = clienteId;
        this.direccionEntrega = direccionEntrega;
        this.responsableTransporte = responsableTransporte;
        this.patenteVehiculo = patenteVehiculo;
        this.estado = "PENDIENTE";
        this.fechaCreacion = LocalDateTime.now();
    }

    public void marcarDespachado() {
        this.estado = "EN_CAMINO";
        this.fechaDespacho = LocalDateTime.now();
    }

    public void marcarEntregado(String observaciones) {
        this.estado = "ENTREGADO";
        this.fechaEntrega = LocalDateTime.now();
        this.observaciones = observaciones;
    }

    public void cancelar(String motivo) {
        this.estado = "CANCELADO";
        this.observaciones = motivo;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCentroDistribucionId() { return centroDistribucionId; }
    public void setCentroDistribucionId(Long centroDistribucionId) { this.centroDistribucionId = centroDistribucionId; }
    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
    public String getDireccionEntrega() { return direccionEntrega; }
    public void setDireccionEntrega(String direccionEntrega) { this.direccionEntrega = direccionEntrega; }
    public String getResponsableTransporte() { return responsableTransporte; }
    public void setResponsableTransporte(String responsableTransporte) { this.responsableTransporte = responsableTransporte; }
    public String getPatenteVehiculo() { return patenteVehiculo; }
    public void setPatenteVehiculo(String patenteVehiculo) { this.patenteVehiculo = patenteVehiculo; }
    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }
    public Integer getCantidadProducto() { return cantidadProducto; }
    public void setCantidadProducto(Integer cantidadProducto) { this.cantidadProducto = cantidadProducto; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public LocalDateTime getFechaDespacho() { return fechaDespacho; }
    public void setFechaDespacho(LocalDateTime fechaDespacho) { this.fechaDespacho = fechaDespacho; }
    public LocalDateTime getFechaEntrega() { return fechaEntrega; }
    public void setFechaEntrega(LocalDateTime fechaEntrega) { this.fechaEntrega = fechaEntrega; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
