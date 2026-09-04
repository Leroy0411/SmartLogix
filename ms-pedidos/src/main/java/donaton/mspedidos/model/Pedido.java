package donaton.mspedidos.model;

import jakarta.persistence.*;

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

    @Column(name = "centro_distribucion_id", nullable = false)
    private Long centroDistribucionId;

    @Column(name = "cliente_id")
    private Long clienteId;

    @Column(name = "direccion_entrega", nullable = false)
    private String direccionEntrega;

    @Column(name = "responsable_transporte")
    private String responsableTransporte;

    @Column(name = "patente_vehiculo")
    private String patenteVehiculo;

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
