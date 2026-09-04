package donaton.msinventario.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

/**
 * Entidad JPA que representa un ítem de inventario en la plataforma SmartLogix.
 * Persistida en base de datos H2 mediante Spring Data JPA (Hibernate).
 * Soporta las categorías: ELECTRONICA, ROPA, HOGAR, ALIMENTOS.
 * Ciclo de vida del stock: DISPONIBLE → RESERVADO → AGOTADO.
 */
@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 20)
    private String categoria;     // ELECTRONICA, ROPA, HOGAR, ALIMENTOS

    @NotBlank
    @Column(nullable = false)
    private String proveedor;

    @Min(1)
    @Column(nullable = false)
    private Integer stock;

    @Column(name = "bodega_id", nullable = false)
    private Long bodegaId;

    @Column(nullable = false, length = 20)
    private String estado;        // DISPONIBLE, RESERVADO, AGOTADO

    @Column(length = 500)
    private String descripcion;

    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDateTime fechaIngreso;

    public Producto() {}

    public Producto(Long id, String categoria, String proveedor, Integer stock,
                    Long bodegaId, String descripcion) {
        this.id = id;
        this.categoria = categoria;
        this.proveedor = proveedor;
        this.stock = stock;
        this.bodegaId = bodegaId;
        this.descripcion = descripcion;
        this.estado = "DISPONIBLE";
        this.fechaIngreso = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getProveedor() { return proveedor; }
    public void setProveedor(String proveedor) { this.proveedor = proveedor; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public Long getBodegaId() { return bodegaId; }
    public void setBodegaId(Long bodegaId) { this.bodegaId = bodegaId; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public LocalDateTime getFechaIngreso() { return fechaIngreso; }
    public void setFechaIngreso(LocalDateTime fechaIngreso) { this.fechaIngreso = fechaIngreso; }
}
