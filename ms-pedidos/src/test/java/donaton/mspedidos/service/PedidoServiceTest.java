package donaton.mspedidos.service;

import donaton.mspedidos.model.CentroDistribucion;
import donaton.mspedidos.model.Pedido;
import donaton.mspedidos.observer.PedidoObserver;
import donaton.mspedidos.repository.CentroDistribucionRepository;
import donaton.mspedidos.repository.PedidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para PedidoService.
 * Verifica el patrón Observer: los observadores son notificados en cada
 * cambio de estado de un pedido (procesamiento + coordinación de envío).
 */
@DisplayName("PedidoService - Pruebas Unitarias")
class PedidoServiceTest {

    @Mock private CentroDistribucionRepository centroRepository;
    @Mock private PedidoRepository pedidoRepository;
    @Mock private PedidoObserver observadorMock;

    private PedidoService pedidoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        pedidoService = new PedidoService(centroRepository, pedidoRepository,
                List.of(observadorMock));
    }

    // ─── OBSERVER: notificación en cambios de estado ─────────────────────

    @Test
    @DisplayName("Crear pedido debe notificar a todos los observadores")
    void crearPedido_notificaObservadores() {
        Pedido pedido = new Pedido(null, 1L, 1L, "Pudahuel", "Juan", "AB-1234");
        when(pedidoRepository.save(any())).thenAnswer(inv -> {
            Pedido p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });

        pedidoService.crearPedido(pedido);

        verify(observadorMock, times(1)).onPedidoActualizado(any(Pedido.class), eq("NUEVO"));
    }

    @Test
    @DisplayName("Despachar pedido debe cambiar estado a EN_CAMINO y notificar observadores")
    void despacharPedido_cambiaEstadoYNotifica() {
        Pedido pedido = new Pedido(1L, 1L, 1L, "Pudahuel", "Juan", "AB-1234");
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any())).thenReturn(pedido);

        Optional<Pedido> resultado = pedidoService.despacharPedido(1L);

        assertTrue(resultado.isPresent());
        assertEquals("EN_CAMINO", resultado.get().getEstado());
        verify(observadorMock).onPedidoActualizado(any(Pedido.class), eq("PENDIENTE"));
    }

    @Test
    @DisplayName("Confirmar entrega debe cambiar estado a ENTREGADO y notificar")
    void confirmarEntrega_cambiaEstadoYNotifica() {
        Pedido pedido = new Pedido(1L, 1L, 1L, "Maipú", "Pedro", "CD-5678");
        pedido.setEstado("EN_CAMINO");
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any())).thenReturn(pedido);

        Optional<Pedido> resultado = pedidoService.confirmarEntrega(1L, "Todo OK");

        assertTrue(resultado.isPresent());
        assertEquals("ENTREGADO", resultado.get().getEstado());
        verify(observadorMock).onPedidoActualizado(any(Pedido.class), eq("EN_CAMINO"));
    }

    @Test
    @DisplayName("Cancelar pedido debe notificar con estado anterior correcto")
    void cancelarPedido_notificaConEstadoAnterior() {
        Pedido pedido = new Pedido(1L, 1L, 1L, "Destino", "Resp", "ZZ-9999");
        pedido.setEstado("PENDIENTE");
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any())).thenReturn(pedido);

        pedidoService.cancelarPedido(1L, "Sin transporte disponible");

        verify(observadorMock).onPedidoActualizado(any(Pedido.class), eq("PENDIENTE"));
        assertEquals("CANCELADO", pedido.getEstado());
    }

    @Test
    @DisplayName("Despachar pedido inexistente debe retornar Optional vacío sin notificar")
    void despacharPedido_inexistente_noNotifica() {
        when(pedidoRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Pedido> resultado = pedidoService.despacharPedido(999L);

        assertFalse(resultado.isPresent());
        verify(observadorMock, never()).onPedidoActualizado(any(), any());
    }

    // ─── CENTROS DE DISTRIBUCIÓN ─────────────────────────────────────────

    @Test
    @DisplayName("ActualizarOcupacion al máximo debe marcar centro como SATURADO")
    void actualizarOcupacion_maxima_marcaSaturado() {
        CentroDistribucion centro = new CentroDistribucion(1L, "Centro A", "Dir", "Santiago",
                100, "Resp", "contacto");
        when(centroRepository.findById(1L)).thenReturn(Optional.of(centro));
        when(centroRepository.save(any())).thenReturn(centro);

        Optional<CentroDistribucion> resultado = pedidoService.actualizarOcupacion(1L, 100);

        assertTrue(resultado.isPresent());
        assertEquals("SATURADO", resultado.get().getEstado());
    }

    @Test
    @DisplayName("ActualizarOcupacion bajo el máximo debe mantener estado ACTIVO")
    void actualizarOcupacion_parcial_mantieneActivo() {
        CentroDistribucion centro = new CentroDistribucion(1L, "Centro B", "Dir", "Maipú",
                100, "Resp", "contacto");
        when(centroRepository.findById(1L)).thenReturn(Optional.of(centro));
        when(centroRepository.save(any())).thenReturn(centro);

        Optional<CentroDistribucion> resultado = pedidoService.actualizarOcupacion(1L, 50);

        assertTrue(resultado.isPresent());
        assertEquals("ACTIVO", resultado.get().getEstado());
    }

    @Test
    @DisplayName("EliminarCentro con ID existente debe retornar true")
    void eliminarCentro_existente_retornaTrue() {
        when(centroRepository.existsById(1L)).thenReturn(true);

        boolean resultado = pedidoService.eliminarCentro(1L);

        assertTrue(resultado);
        verify(centroRepository).deleteById(1L);
    }

    @Test
    @DisplayName("EliminarCentro con ID inexistente debe retornar false")
    void eliminarCentro_inexistente_retornaFalse() {
        when(centroRepository.existsById(999L)).thenReturn(false);

        boolean resultado = pedidoService.eliminarCentro(999L);

        assertFalse(resultado);
        verify(centroRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("EliminarPedido con ID existente debe retornar true")
    void eliminarPedido_existente_retornaTrue() {
        when(pedidoRepository.existsById(1L)).thenReturn(true);

        boolean resultado = pedidoService.eliminarPedido(1L);

        assertTrue(resultado);
        verify(pedidoRepository).deleteById(1L);
    }

    @Test
    @DisplayName("EliminarPedido con ID inexistente debe retornar false")
    void eliminarPedido_inexistente_retornaFalse() {
        when(pedidoRepository.existsById(999L)).thenReturn(false);

        boolean resultado = pedidoService.eliminarPedido(999L);

        assertFalse(resultado);
        verify(pedidoRepository, never()).deleteById(any());
    }
}
