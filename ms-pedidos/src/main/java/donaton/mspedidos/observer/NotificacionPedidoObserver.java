package donaton.mspedidos.observer;

import donaton.mspedidos.model.Pedido;
import org.springframework.stereotype.Component;

/**
 * Observador de notificaciones: simula el disparo de alertas al cliente
 * y al transportista cuando el estado del pedido cambia.
 *
 * En producción publicaría un evento en RabbitMQ que MS-Notificaciones consumiría.
 */
@Component
public class NotificacionPedidoObserver implements PedidoObserver {

    @Override
    public void onPedidoActualizado(Pedido pedido, String estadoAnterior) {
        switch (pedido.getEstado()) {
            case "EN_CAMINO" ->
                System.out.printf("[NOTIFICACIÓN] SMS al transportista %s: El pedido #%d va en camino hacia %s%n",
                        pedido.getResponsableTransporte(), pedido.getId(), pedido.getDireccionEntrega());
            case "ENTREGADO" ->
                System.out.printf("[NOTIFICACIÓN] Email confirmación: Pedido #%d entregado en %s%n",
                        pedido.getId(), pedido.getDireccionEntrega());
            case "CANCELADO" ->
                System.out.printf("[NOTIFICACIÓN] Alerta: Pedido #%d cancelado. Observaciones: %s%n",
                        pedido.getId(), pedido.getObservaciones());
            default ->
                System.out.printf("[NOTIFICACIÓN] Pedido #%d actualizó estado a %s%n",
                        pedido.getId(), pedido.getEstado());
        }
    }
}
