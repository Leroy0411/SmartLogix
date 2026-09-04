package donaton.msinventario.factory;

import donaton.msinventario.model.Producto;

/**
 * PATRÓN: Factory Method (GoF - Creacional)
 * ─────────────────────────────────────────────────────────────────────────
 * Define la interfaz para crear objetos Producto, delegando la lógica de
 * construcción y validación a las subclases concretas según la categoría
 * del producto (Electrónica, Ropa, Hogar, Alimentos).
 *
 * Beneficio: agregar una nueva categoría de producto solo requiere
 * implementar una nueva subclase, sin modificar código existente
 * (Open/Closed Principle).
 */
public abstract class ProductoFactory {

    /**
     * Factory Method: crea y valida un producto de la categoría correspondiente.
     */
    public final Producto crear(String proveedor, Integer stock,
                                Long bodegaId, String descripcion) {
        validarParametros(proveedor, stock);
        Producto producto = construir(proveedor, stock, bodegaId, descripcion);
        aplicarReglasCategoria(producto);
        return producto;
    }

    /** Construye la instancia concreta con la categoría definida por la subclase. */
    protected abstract Producto construir(String proveedor, Integer stock,
                                          Long bodegaId, String descripcion);

    /** Aplica reglas de negocio específicas a la categoría del producto. */
    protected abstract void aplicarReglasCategoria(Producto producto);

    /** Categoría de producto que produce esta fábrica. */
    public abstract String getCategoria();

    private void validarParametros(String proveedor, Integer stock) {
        if (proveedor == null || proveedor.isBlank()) {
            throw new IllegalArgumentException("El proveedor del producto es requerido");
        }
        if (stock == null || stock <= 0) {
            throw new IllegalArgumentException("El stock debe ser mayor a cero");
        }
    }
}
