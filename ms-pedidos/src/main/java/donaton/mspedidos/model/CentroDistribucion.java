package donaton.mspedidos.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Entidad JPA que representa un centro de distribución en la plataforma
 * SmartLogix, desde donde se despachan los pedidos hacia los clientes.
 * Persistida en base de datos H2 mediante Spring Data JPA (Hibernate).
 */
@Entity
@Table(name = "centros_distribucion")
public class CentroDistribucion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String direccion;

    @Column(nullable = false)
    private String comuna;

    @Min(1)
    @Column(name = "capacidad_maxima", nullable = false)
    private Integer capacidadMaxima;

    @Column(name = "ocupacion_actual", nullable = false)
    private Integer ocupacionActual;

    @Column(nullable = false, length = 20)
    private String estado;              // ACTIVO, INACTIVO, SATURADO

    @Column(name = "responsable_nombre")
    private String responsableNombre;

    @Column(name = "responsable_contacto")
    private String responsableContacto;

    public CentroDistribucion() {}

    public CentroDistribucion(Long id, String nombre, String direccion, String comuna,
                        Integer capacidadMaxima, String responsableNombre, String responsableContacto) {
        this.id = id;
        this.nombre = nombre;
        this.direccion = direccion;
        this.comuna = comuna;
        this.capacidadMaxima = capacidadMaxima;
        this.ocupacionActual = 0;
        this.estado = "ACTIVO";
        this.responsableNombre = responsableNombre;
        this.responsableContacto = responsableContacto;
    }

    @Transient
    public boolean tieneCapacidadDisponible() {
        return ocupacionActual < capacidadMaxima;
    }

    @Transient
    public int getCapacidadDisponible() {
        return capacidadMaxima - ocupacionActual;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getComuna() { return comuna; }
    public void setComuna(String comuna) { this.comuna = comuna; }
    public Integer getCapacidadMaxima() { return capacidadMaxima; }
    public void setCapacidadMaxima(Integer capacidadMaxima) { this.capacidadMaxima = capacidadMaxima; }
    public Integer getOcupacionActual() { return ocupacionActual; }
    public void setOcupacionActual(Integer ocupacionActual) { this.ocupacionActual = ocupacionActual; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getResponsableNombre() { return responsableNombre; }
    public void setResponsableNombre(String responsableNombre) { this.responsableNombre = responsableNombre; }
    public String getResponsableContacto() { return responsableContacto; }
    public void setResponsableContacto(String responsableContacto) { this.responsableContacto = responsableContacto; }
}
