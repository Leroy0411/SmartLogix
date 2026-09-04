package donaton.mspedidos.service;

import donaton.mspedidos.client.InventarioClient;
import donaton.mspedidos.model.CentroDistribucion;
import donaton.mspedidos.model.Pedido;
import donaton.mspedidos.observer.PedidoObserver;
import donaton.mspedidos.repository.CentroDistribucionRepository;
import donaton.mspedidos.repository.PedidoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de procesamiento de pedidos y coordinación de envíos (SmartLogix).
 *
 * Cubre los módulos "Procesamiento de Pedidos" (validación, aprobación y
 * asignación de pedidos, trazabilidad) y "Coordinación de Envíos"
 * (planificación de despacho y comunicación con transportistas) del caso.
 *
 * Aplica el patrón Observer: cada cambio de estado en un pedido
 * notifica automáticamente a todos los observadores registrados
 * (auditoría, notificaciones, métricas, etc.), garantizando trazabilidad.
 *
 * Ciclo de vida de un pedido (validado explícitamente, ver métodos de abajo):
 * <pre>
 *   PENDIENTE ──despachar──▶ EN_CAMINO ──entregar──▶ ENTREGADO
 *       └──────────cancelar──────────▶ CANCELADO
 * </pre>
 * Un pedido ya EN_CAMINO no puede cancelarse desde este servicio (debería
 * gestionarse como una devolución, fuera del alcance de esta entrega), y
 * ninguna transición puede aplicarse dos veces.
 */
@Service
public class PedidoService {

    private static final Logger log = LoggerFactory.getLogger(PedidoService.class);

    private static final String PENDIENTE = "PENDIENTE";
    private static final String EN_CAMINO = "EN_CAMINO";
    private static final String ENTREGADO = "ENTREGADO";
    private static final String CANCELADO = "CANCELADO";

    private final CentroDistribucionRepository centroRepository;
    private final PedidoRepository pedidoRepository;
    private final List<PedidoObserver> observadores;  // Inyectados por Spring
    private final InventarioClient inventarioClient;

    public PedidoService(CentroDistribucionRepository centroRepository,
                             PedidoRepository pedidoRepository,
                             List<PedidoObserver> observadores,
                             InventarioClient inventarioClient) {
        this.centroRepository = centroRepository;
        this.pedidoRepository  = pedidoRepository;
        this.observadores     = observadores;
        this.inventarioClient = inventarioClient;
    }

    // ── Centros de Distribución ────────────────────────────────────────────

    public List<CentroDistribucion> obtenerCentros() {
        return centroRepository.findAll();
    }

    public List<CentroDistribucion> obtenerCentrosActivos() {
        return centroRepository.findByEstado("ACTIVO");
    }

    public List<CentroDistribucion> obtenerCentrosConCapacidad() {
        return centroRepository.findAll().stream()
                .filter(c -> "ACTIVO".equalsIgnoreCase(c.getEstado()) && c.tieneCapacidadDisponible())
                .toList();
    }

    public CentroDistribucion agregarCentro(CentroDistribucion centro) {
        if (centro.getEstado() == null || centro.getEstado().isBlank()) {
            centro.setEstado("ACTIVO");
        }
        if (centro.getOcupacionActual() == null) {
            centro.setOcupacionActual(0);
        }
        return centroRepository.save(centro);
    }

    public Optional<CentroDistribucion> actualizarOcupacion(Long id, Integer nuevaOcupacion) {
        return centroRepository.findById(id).map(centro -> {
            centro.setOcupacionActual(nuevaOcupacion);
            centro.setEstado(nuevaOcupacion >= centro.getCapacidadMaxima() ? "SATURADO" : "ACTIVO");
            return centroRepository.save(centro);
        });
    }

    public boolean eliminarCentro(Long id) {
        if (!centroRepository.existsById(id)) {
            return false;
        }
        centroRepository.deleteById(id);
        return true;
    }

    // ── Pedidos con patrón Observer ──────────────────────────────────────

    public List<Pedido> obtenerPedidos() {
        return pedidoRepository.findAll();
    }

    public List<Pedido> obtenerPorEstado(String estado) {
        return pedidoRepository.findByEstado(estado.toUpperCase());
    }

    /**
     * Crea un pedido y reserva capacidad en su centro de distribución.
     *
     * @throws IllegalArgumentException si el centro de distribución no existe.
     * @throws IllegalStateException si el centro no está ACTIVO o no tiene
     *         capacidad disponible — antes esto no se validaba nunca y un
     *         centro podía quedar sobre-asignado sin que nada lo impidiera.
     */
    public Pedido crearPedido(Pedido pedido) {
        CentroDistribucion centro = centroRepository.findById(pedido.getCentroDistribucionId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Centro de distribución no encontrado: " + pedido.getCentroDistribucionId()));

        if (!"ACTIVO".equalsIgnoreCase(centro.getEstado())) {
            throw new IllegalStateException(
                    "El centro de distribución " + centro.getId() + " no está ACTIVO (estado actual: " + centro.getEstado() + ")");
        }
        if (!centro.tieneCapacidadDisponible()) {
            throw new IllegalStateException(
                    "El centro de distribución " + centro.getId() + " no tiene capacidad disponible");
        }

        reservarEspacio(centro);

        pedido.setEstado(PENDIENTE);
        Pedido creado = pedidoRepository.save(pedido);
        notificarObservadores(creado, "NUEVO");
        return creado;
    }

    /**
     * Despacha un pedido PENDIENTE: lo marca EN_CAMINO, libera el espacio
     * reservado en el centro de distribución y descuenta el stock
     * correspondiente en MS-Inventario (si el pedido tiene un producto
     * asociado).
     *
     * @throws IllegalStateException si el pedido no está en estado PENDIENTE.
     */
    public Optional<Pedido> despacharPedido(Long id) {
        return pedidoRepository.findById(id).map(pedido -> {
            exigirEstado(pedido, PENDIENTE, "despachar");

            String estadoAnterior = pedido.getEstado();
            pedido.marcarDespachado();
            pedidoRepository.save(pedido);

            centroRepository.findById(pedido.getCentroDistribucionId())
                    .ifPresent(this::liberarEspacio);

            if (pedido.getProductoId() != null && pedido.getCantidadProducto() != null) {
                boolean descontado = inventarioClient.descontarStock(pedido.getProductoId(), pedido.getCantidadProducto());
                if (!descontado) {
                    log.warn("Pedido #{} despachado, pero MS-Inventario no confirmó el descuento de stock del producto {}",
                            pedido.getId(), pedido.getProductoId());
                }
            }

            notificarObservadores(pedido, estadoAnterior);
            return pedido;
        });
    }

    /**
     * Confirma la entrega de un pedido EN_CAMINO.
     *
     * @throws IllegalStateException si el pedido no está en estado EN_CAMINO.
     */
    public Optional<Pedido> confirmarEntrega(Long id, String observaciones) {
        return pedidoRepository.findById(id).map(pedido -> {
            exigirEstado(pedido, EN_CAMINO, "entregar");

            String estadoAnterior = pedido.getEstado();
            pedido.marcarEntregado(observaciones);
            pedidoRepository.save(pedido);
            notificarObservadores(pedido, estadoAnterior);
            return pedido;
        });
    }

    /**
     * Cancela un pedido que aún no ha sido despachado y libera la capacidad
     * reservada en su centro de distribución.
     *
     * @throws IllegalStateException si el pedido ya está EN_CAMINO, ENTREGADO
     *         o CANCELADO — un pedido en camino debe gestionarse como
     *         devolución, no como cancelación (fuera del alcance actual).
     */
    public Optional<Pedido> cancelarPedido(Long id, String motivo) {
        return pedidoRepository.findById(id).map(pedido -> {
            exigirEstado(pedido, PENDIENTE, "cancelar");

            String estadoAnterior = pedido.getEstado();
            pedido.cancelar(motivo);
            pedidoRepository.save(pedido);

            centroRepository.findById(pedido.getCentroDistribucionId())
                    .ifPresent(this::liberarEspacio);

            notificarObservadores(pedido, estadoAnterior);
            return pedido;
        });
    }

    public boolean eliminarPedido(Long id) {
        if (!pedidoRepository.existsById(id)) {
            return false;
        }
        pedidoRepository.deleteById(id);
        return true;
    }

    // ── Helpers de negocio ────────────────────────────────────────────────

    private void exigirEstado(Pedido pedido, String estadoRequerido, String accion) {
        if (!estadoRequerido.equalsIgnoreCase(pedido.getEstado())) {
            throw new IllegalStateException(
                    "No se puede " + accion + " el pedido #" + pedido.getId() +
                            ": está en estado " + pedido.getEstado() + " (se requiere " + estadoRequerido + ")");
        }
    }

    private void reservarEspacio(CentroDistribucion centro) {
        centro.setOcupacionActual(centro.getOcupacionActual() + 1);
        if (!centro.tieneCapacidadDisponible()) {
            centro.setEstado("SATURADO");
        }
        centroRepository.save(centro);
    }

    private void liberarEspacio(CentroDistribucion centro) {
        int nuevaOcupacion = Math.max(0, centro.getOcupacionActual() - 1);
        centro.setOcupacionActual(nuevaOcupacion);
        if ("SATURADO".equalsIgnoreCase(centro.getEstado()) && centro.tieneCapacidadDisponible()) {
            centro.setEstado("ACTIVO");
        }
        centroRepository.save(centro);
    }

    /**
     * Notifica a todos los observadores registrados.
     * La adición de nuevos observadores no requiere modificar este método.
     */
    private void notificarObservadores(Pedido pedido, String estadoAnterior) {
        for (PedidoObserver obs : observadores) {
            obs.onPedidoActualizado(pedido, estadoAnterior);
        }
    }
}
