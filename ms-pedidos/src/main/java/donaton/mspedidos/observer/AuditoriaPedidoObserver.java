package donaton.mspedidos.observer;

import donaton.mspedidos.model.Pedido;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// ══════════════════════════════════════════════════════════
//  Observadores concretos — reaccionan a cambios de estado
// ══════════════════════════════════════════════════════════

/**
 * Observador de auditoría: registra todos los cambios de estado de pedidos,
 * dando trazabilidad al procesamiento y despacho de cada pedido.
 */
@Component
public class AuditoriaPedidoObserver implements PedidoObserver {

    // En producción esto se persistiría en base de datos
    private final List<String> bitacora = new ArrayList<>();

    @Override
    public void onPedidoActualizado(Pedido pedido, String estadoAnterior) {
        String entrada = String.format("[%s] Pedido #%d cambió de %s → %s (entrega: %s)",
                LocalDateTime.now(), pedido.getId(), estadoAnterior,
                pedido.getEstado(), pedido.getDireccionEntrega());
        bitacora.add(entrada);
        System.out.println("[AUDITORÍA] " + entrada);
    }

    public List<String> getBitacora() {
        return new ArrayList<>(bitacora);
    }
}
