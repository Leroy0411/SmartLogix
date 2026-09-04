package donaton.msinventario.factory;

import donaton.msinventario.model.Producto;
import org.springframework.stereotype.Component;

// ══════════════════════════════════════════════════════════
//  Fábricas concretas — una por categoría de producto
//  Cada subclase encapsula sus propias reglas de negocio
// ══════════════════════════════════════════════════════════

/** Fábrica para productos de electrónica */
@Component
class ProductoElectronicaFactory extends ProductoFactory {

    @Override
    public String getCategoria() { return "ELECTRONICA"; }

    @Override
    protected Producto construir(String proveedor, Integer stock,
                                 Long bodegaId, String descripcion) {
        return new Producto(null, "ELECTRONICA", proveedor, stock, bodegaId, descripcion);
    }

    @Override
    protected void aplicarReglasCategoria(Producto producto) {
        // Regla de negocio: lotes de electrónica mayores a 100 unidades quedan
        // RESERVADOS hasta que control de calidad valide el ingreso a bodega.
        if (producto.getStock() >= 100) {
            producto.setEstado("RESERVADO");
        }
    }
}

/** Fábrica para productos de alimentos */
@Component
class ProductoAlimentosFactory extends ProductoFactory {

    @Override
    public String getCategoria() { return "ALIMENTOS"; }

    @Override
    protected Producto construir(String proveedor, Integer stock,
                                 Long bodegaId, String descripcion) {
        return new Producto(null, "ALIMENTOS", proveedor, stock, bodegaId, descripcion);
    }

    @Override
    protected void aplicarReglasCategoria(Producto producto) {
        // Regla de negocio: alimentos siempre inician DISPONIBLES, sujetos a
        // control de vencimiento en el módulo de rotación de inventario.
        producto.setEstado("DISPONIBLE");
    }
}

/** Fábrica para productos de hogar */
@Component
class ProductoHogarFactory extends ProductoFactory {

    @Override
    public String getCategoria() { return "HOGAR"; }

    @Override
    protected Producto construir(String proveedor, Integer stock,
                                 Long bodegaId, String descripcion) {
        return new Producto(null, "HOGAR", proveedor, stock, bodegaId, descripcion);
    }

    @Override
    protected void aplicarReglasCategoria(Producto producto) {
        // Regla de negocio: productos de hogar de alta rotación quedan
        // RESERVADOS para picking prioritario.
        producto.setEstado("RESERVADO");
    }
}

/** Fábrica para productos de vestuario (ropa) */
@Component
class ProductoRopaFactory extends ProductoFactory {

    @Override
    public String getCategoria() { return "ROPA"; }

    @Override
    protected Producto construir(String proveedor, Integer stock,
                                 Long bodegaId, String descripcion) {
        return new Producto(null, "ROPA", proveedor, stock, bodegaId, descripcion);
    }

    @Override
    protected void aplicarReglasCategoria(Producto producto) {
        producto.setEstado("DISPONIBLE");
    }
}
