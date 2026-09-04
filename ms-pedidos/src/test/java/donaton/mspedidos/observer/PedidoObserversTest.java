package donaton.mspedidos.observer;

import donaton.mspedidos.model.Pedido;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para los observadores concretos (patrón Observer).
 * Verifica que cada observador reacciona correctamente a los cambios
 * de estado de un pedido.
 */
@DisplayName("Observadores de Pedido - Pruebas Unitarias")
class PedidoObserversTest {

    @Test
    @DisplayName("AuditoriaPedidoObserver registra cada cambio de estado en su bitácora")
    void auditoriaObserver_registraEnBitacora() {
        AuditoriaPedidoObserver observer = new AuditoriaPedidoObserver();
        Pedido pedido = new Pedido(1L, 1L, 1L, "Pudahuel", "Juan", "AB-1234");
        pedido.setEstado("EN_CAMINO");

        observer.onPedidoActualizado(pedido, "PENDIENTE");

        assertEquals(1, observer.getBitacora().size());
        assertTrue(observer.getBitacora().get(0).contains("PENDIENTE"));
        assertTrue(observer.getBitacora().get(0).contains("EN_CAMINO"));
    }

    @Test
    @DisplayName("AuditoriaPedidoObserver acumula múltiples entradas en orden")
    void auditoriaObserver_acumulaMultiplesEntradas() {
        AuditoriaPedidoObserver observer = new AuditoriaPedidoObserver();
        Pedido pedido = new Pedido(1L, 1L, 1L, "Pudahuel", "Juan", "AB-1234");

        observer.onPedidoActualizado(pedido, "PENDIENTE");
        pedido.setEstado("ENTREGADO");
        observer.onPedidoActualizado(pedido, "EN_CAMINO");

        assertEquals(2, observer.getBitacora().size());
    }

    @Test
    @DisplayName("NotificacionPedidoObserver no lanza excepción al notificar EN_CAMINO")
    void notificacionObserver_enCamino_noLanzaExcepcion() {
        NotificacionPedidoObserver observer = new NotificacionPedidoObserver();
        Pedido pedido = new Pedido(1L, 1L, 1L, "Pudahuel", "Juan", "AB-1234");
        pedido.setEstado("EN_CAMINO");

        assertDoesNotThrow(() -> observer.onPedidoActualizado(pedido, "PENDIENTE"));
    }

    @Test
    @DisplayName("NotificacionPedidoObserver no lanza excepción al notificar ENTREGADO")
    void notificacionObserver_entregado_noLanzaExcepcion() {
        NotificacionPedidoObserver observer = new NotificacionPedidoObserver();
        Pedido pedido = new Pedido(1L, 1L, 1L, "Pudahuel", "Juan", "AB-1234");
        pedido.setEstado("ENTREGADO");

        assertDoesNotThrow(() -> observer.onPedidoActualizado(pedido, "EN_CAMINO"));
    }

    @Test
    @DisplayName("NotificacionPedidoObserver no lanza excepción al notificar CANCELADO")
    void notificacionObserver_cancelado_noLanzaExcepcion() {
        NotificacionPedidoObserver observer = new NotificacionPedidoObserver();
        Pedido pedido = new Pedido(1L, 1L, 1L, "Pudahuel", "Juan", "AB-1234");
        pedido.cancelar("Sin transporte disponible");

        assertDoesNotThrow(() -> observer.onPedidoActualizado(pedido, "PENDIENTE"));
    }

    @Test
    @DisplayName("NotificacionPedidoObserver maneja el caso por defecto sin lanzar excepción")
    void notificacionObserver_estadoDesconocido_noLanzaExcepcion() {
        NotificacionPedidoObserver observer = new NotificacionPedidoObserver();
        Pedido pedido = new Pedido(1L, 1L, 1L, "Pudahuel", "Juan", "AB-1234");
        pedido.setEstado("PENDIENTE");

        assertDoesNotThrow(() -> observer.onPedidoActualizado(pedido, "NUEVO"));
    }
}
