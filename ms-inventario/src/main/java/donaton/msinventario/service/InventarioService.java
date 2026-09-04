package donaton.msinventario.service;

import donaton.msinventario.factory.ProductoFactoryProvider;
import donaton.msinventario.model.Producto;
import donaton.msinventario.repository.ProductoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de negocio para gestión de inventario (SmartLogix).
 *
 * Orquesta el uso del Repository Pattern (acceso a datos desacoplado)
 * y el Factory Method (creación de productos por categoría), dando
 * soporte al módulo "Gestión de Inventario" del caso: mantiene
 * actualizados los niveles de stock por bodega en tiempo real.
 */
@Service
public class InventarioService {

    private final ProductoRepository productoRepository;
    private final ProductoFactoryProvider factoryProvider;

    public InventarioService(ProductoRepository productoRepository,
                             ProductoFactoryProvider factoryProvider) {
        this.productoRepository = productoRepository;
        this.factoryProvider = factoryProvider;
    }

    public List<Producto> obtenerTodos() {
        return productoRepository.findAll();
    }

    public Optional<Producto> obtenerPorId(Long id) {
        return productoRepository.findById(id);
    }

    public List<Producto> obtenerPorEstado(String estado) {
        return productoRepository.findByEstado(estado.toUpperCase());
    }

    public List<Producto> obtenerPorBodega(Long bodegaId) {
        return productoRepository.findByBodegaId(bodegaId);
    }

    /**
     * Da de alta un producto en inventario usando el Factory Method
     * correspondiente a su categoría. La fábrica aplica las reglas de
     * negocio específicas de cada categoría.
     */
    public Producto crearProducto(String categoria, String proveedor, Integer stock,
                                  Long bodegaId, String descripcion) {
        Producto producto = factoryProvider
                .obtenerFabrica(categoria)
                .crear(proveedor, stock, bodegaId, descripcion);
        return productoRepository.save(producto);
    }

    public Optional<Producto> actualizarEstado(Long id, String nuevoEstado) {
        Optional<Producto> opt = productoRepository.findById(id);
        if (opt.isPresent()) {
            Producto p = opt.get();
            p.setEstado(nuevoEstado.toUpperCase());
            productoRepository.save(p);
            return Optional.of(p);
        }
        return Optional.empty();
    }

    /**
     * Descuenta stock de un producto (por ejemplo, al confirmarse un pedido
     * en MS-Pedidos) y lo marca AGOTADO si llega a cero.
     */
    public Optional<Producto> descontarStock(Long id, Integer cantidad) {
        return productoRepository.findById(id).map(producto -> {
            int nuevoStock = Math.max(0, producto.getStock() - cantidad);
            producto.setStock(nuevoStock);
            producto.setEstado(nuevoStock == 0 ? "AGOTADO" : producto.getEstado());
            return productoRepository.save(producto);
        });
    }

    public boolean eliminar(Long id) {
        if (!productoRepository.existsById(id)) {
            return false;
        }
        productoRepository.deleteById(id);
        return true;
    }
}
