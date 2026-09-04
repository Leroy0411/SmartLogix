package donaton.msinventario.service;

import donaton.msinventario.factory.ProductoFactoryProvider;
import donaton.msinventario.model.Producto;
import donaton.msinventario.repository.ProductoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

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

    /** Únicos estados válidos del ciclo de vida de un producto. */
    private static final Set<String> ESTADOS_VALIDOS = Set.of("DISPONIBLE", "RESERVADO", "AGOTADO");

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

    /**
     * @throws IllegalArgumentException si {@code nuevoEstado} no es uno de los
     *         estados válidos (DISPONIBLE, RESERVADO, AGOTADO). Antes cualquier
     *         string se aceptaba sin validar, dejando el dato inconsistente.
     */
    public Optional<Producto> actualizarEstado(Long id, String nuevoEstado) {
        String estadoNormalizado = validarEstado(nuevoEstado);
        return productoRepository.findById(id).map(p -> {
            p.setEstado(estadoNormalizado);
            return productoRepository.save(p);
        });
    }

    /**
     * Descuenta stock de un producto (por ejemplo, al despacharse un pedido
     * en MS-Pedidos) y lo marca AGOTADO si llega a cero.
     *
     * @throws IllegalArgumentException si la cantidad a descontar no es positiva.
     */
    public Optional<Producto> descontarStock(Long id, Integer cantidad) {
        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a descontar debe ser mayor que cero");
        }
        return productoRepository.findById(id).map(producto -> {
            int nuevoStock = Math.max(0, producto.getStock() - cantidad);
            producto.setStock(nuevoStock);
            producto.setEstado(nuevoStock == 0 ? "AGOTADO" : producto.getEstado());
            return productoRepository.save(producto);
        });
    }

    private String validarEstado(String estado) {
        String normalizado = estado == null ? "" : estado.toUpperCase();
        if (!ESTADOS_VALIDOS.contains(normalizado)) {
            throw new IllegalArgumentException(
                    "Estado no válido: " + estado + ". Estados válidos: " + ESTADOS_VALIDOS);
        }
        return normalizado;
    }

    public boolean eliminar(Long id) {
        if (!productoRepository.existsById(id)) {
            return false;
        }
        productoRepository.deleteById(id);
        return true;
    }
}
