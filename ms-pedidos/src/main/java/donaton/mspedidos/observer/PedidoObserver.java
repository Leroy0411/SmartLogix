package donaton.mspedidos.observer;

import donaton.mspedidos.model.Pedido;

/**
 * PATRÓN: Observer (GoF - Comportamiento)
 * ─────────────────────────────────────────────────────────────────────────
 * Define el contrato para los observadores que reaccionan a cambios
 * de estado en los pedidos de la plataforma SmartLogix (procesamiento
 * del pedido y coordinación de su envío).
 *
 * Beneficio: desacopla el sujeto (PedidoService) de sus dependientes
 * (notificaciones al cliente/transportista, auditoría, métricas). Nuevas
 * reacciones se agregan implementando esta interfaz sin modificar código
 * existente.
 */
public interface PedidoObserver {

    /**
     * Notifica al observador que el estado de un pedido ha cambiado.
     * @param pedido   el pedido que cambió de estado
     * @param estadoAnterior  estado previo del pedido
     */
    void onPedidoActualizado(Pedido pedido, String estadoAnterior);
}
