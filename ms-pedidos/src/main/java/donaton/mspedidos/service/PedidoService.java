package donaton.mspedidos.service;

import donaton.mspedidos.model.CentroDistribucion;
import donaton.mspedidos.model.Pedido;
import donaton.mspedidos.observer.PedidoObserver;
import donaton.mspedidos.repository.CentroDistribucionRepository;
import donaton.mspedidos.repository.PedidoRepository;
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
 */
@Service
public class PedidoService {

    private final CentroDistribucionRepository centroRepository;
    private final PedidoRepository pedidoRepository;
    private final List<PedidoObserver> observadores;  // Inyectados por Spring

    public PedidoService(CentroDistribucionRepository centroRepository,
                             PedidoRepository pedidoRepository,
                             List<PedidoObserver> observadores) {
        this.centroRepository = centroRepository;
        this.pedidoRepository  = pedidoRepository;
        this.observadores     = observadores;
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

    public Pedido crearPedido(Pedido pedido) {
        pedido.setEstado("PENDIENTE");
        Pedido creado = pedidoRepository.save(pedido);
        notificarObservadores(creado, "NUEVO");
        return creado;
    }

    public Optional<Pedido> despacharPedido(Long id) {
        return pedidoRepository.findById(id).map(pedido -> {
            String estadoAnterior = pedido.getEstado();
            pedido.marcarDespachado();
            pedidoRepository.save(pedido);
            notificarObservadores(pedido, estadoAnterior);
            return pedido;
        });
    }

    public Optional<Pedido> confirmarEntrega(Long id, String observaciones) {
        return pedidoRepository.findById(id).map(pedido -> {
            String estadoAnterior = pedido.getEstado();
            pedido.marcarEntregado(observaciones);
            pedidoRepository.save(pedido);
            notificarObservadores(pedido, estadoAnterior);
            return pedido;
        });
    }

    public Optional<Pedido> cancelarPedido(Long id, String motivo) {
        return pedidoRepository.findById(id).map(pedido -> {
            String estadoAnterior = pedido.getEstado();
            pedido.cancelar(motivo);
            pedidoRepository.save(pedido);
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
