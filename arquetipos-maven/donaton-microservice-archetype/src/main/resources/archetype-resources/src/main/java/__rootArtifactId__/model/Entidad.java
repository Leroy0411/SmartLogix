package ${package}.model;

import java.time.LocalDateTime;

/**
 * Entidad de dominio base generada por el arquetipo Donaton.
 * Renombrar y adaptar atributos según el dominio del microservicio.
 */
public class Entidad {

    private Long id;
    private String nombre;
    private String estado;   // ACTIVO, INACTIVO
    private LocalDateTime fechaCreacion;

    public Entidad() {}

    public Entidad(Long id, String nombre) {
        this.id = id;
        this.nombre = nombre;
        this.estado = "ACTIVO";
        this.fechaCreacion = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
