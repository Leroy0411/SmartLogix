package donaton.msinventario.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO de entrada para el alta de un producto.
 * Separa el contrato HTTP de la entidad JPA (Producto) para poder aplicar
 * Bean Validation (@Valid) en el controlador — antes el endpoint recibía
 * un {@code Map<String,Object>} crudo y las anotaciones @NotBlank/@Min de
 * la entidad nunca se llegaban a evaluar en la capa web.
 */
public class ProductoCreateRequest {

    @NotBlank(message = "La categoría es obligatoria")
    private String categoria;

    @NotBlank(message = "El proveedor es obligatorio")
    private String proveedor;

    @NotNull(message = "El stock es obligatorio")
    @Min(value = 1, message = "El stock debe ser al menos 1")
    private Integer stock;

    @NotNull(message = "La bodega es obligatoria")
    private Long bodegaId;

    private String descripcion;

    public ProductoCreateRequest() {}

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getProveedor() { return proveedor; }
    public void setProveedor(String proveedor) { this.proveedor = proveedor; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public Long getBodegaId() { return bodegaId; }
    public void setBodegaId(Long bodegaId) { this.bodegaId = bodegaId; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
