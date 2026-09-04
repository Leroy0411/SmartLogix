package donaton.mspedidos.service;

import donaton.mspedidos.client.InventarioClient;
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
 * Verifica el patrón Observer (los observadores son notificados en cada
 * cambio de estado), la reserva/liberación de capacidad en el centro de
 * distribución, las transiciones de estado válidas del pedido y la
 * integración con MS-Inventario al despachar.
 */
@DisplayName("PedidoService - Pruebas Unitarias")
class PedidoServiceTest {

    @Mock private CentroDistribucionRepository centroRepository;
    @Mock private PedidoRepository pedidoRepository;
    @Mock private PedidoObserver observadorMock;
    @Mock private InventarioClient inventarioClient;

    private PedidoService pedidoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        pedidoService = new PedidoService(centroRepository, pedidoRepository,
                List.of(observadorMock), inventarioClient);
    }

    private CentroDistribucion centroActivoConCapacidad() {
        CentroDistribucion centro = new CentroDistribucion(1L, "Centro A", "Dir", "Santiago",
                10, "Resp", "contacto");
        centro.setEstado("ACTIVO");
        centro.setOcupacionActual(0);
        return centro;
    }

    // ─── CREACIÓN Y RESERVA DE CAPACIDAD ─────────────────────────────────

    @Test
    @DisplayName("Crear pedido debe notificar a todos los observadores")
    void crearPedido_notificaObservadores() {
        Pedido pedido = new Pedido(null, 1L, 1L, "Pudahuel", "Juan", "AB-1234");
        when(centroRepository.findById(1L)).thenReturn(Optional.of(centroActivoConCapacidad()));
        when(pedidoRepository.save(any())).thenAnswer(inv -> {
            Pedido p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });

        pedidoService.crearPedido(pedido);

        verify(observadorMock, times(1)).onPedidoActualizado(any(Pedido.class), eq("NUEVO"));
    }

    @Test
    @DisplayName("Crear pedido reserva un espacio en el centro de distribución")
    void crearPedido_reservaEspacioEnElCentro() {
        Pedido pedido = new Pedido(null, 1L, 1L, "Pudahuel", "Juan", "AB-1234");
        CentroDistribucion centro = centroActivoConCapacidad();
        when(centroRepository.findById(1L)).thenReturn(Optional.of(centro));
        when(pedidoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        pedidoService.crearPedido(pedido);

        assertEquals(1, centro.getOcupacionActual());
        verify(centroRepository).save(centro);
    }

    @Test
    @DisplayName("Crear pedido con centro inexistente lanza IllegalArgumentException")
    void crearPedido_centroInexistente_lanzaExcepcion() {
        Pedido pedido = new Pedido(null, 99L, 1L, "Pudahuel", "Juan", "AB-1234");
        when(centroRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> pedidoService.crearPedido(pedido));
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Crear pedido sin capacidad disponible en el centro lanza IllegalStateException")
    void crearPedido_centroSinCapacidad_lanzaExcepcion() {
        Pedido pedido = new Pedido(null, 1L, 1L, "Pudahuel", "Juan", "AB-1234");
        CentroDistribucion centroLleno = new CentroDistribucion(1L, "Centro A", "Dir", "Santiago",
                1, "Resp", "contacto");
        centroLleno.setEstado("ACTIVO");
        centroLleno.setOcupacionActual(1); // ya está en su capacidad máxima
        when(centroRepository.findById(1L)).thenReturn(Optional.of(centroLleno));

        assertThrows(IllegalStateException.class, () -> pedidoService.crearPedido(pedido));
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Crear pedido en un centro INACTIVO lanza IllegalStateException")
    void crearPedido_centroInactivo_lanzaExcepcion() {
        Pedido pedido = new Pedido(null, 1L, 1L, "Pudahuel", "Juan", "AB-1234");
        CentroDistribucion centroInactivo = centroActivoConCapacidad();
        centroInactivo.setEstado("INACTIVO");
        when(centroRepository.findById(1L)).thenReturn(Optional.of(centroInactivo));

        assertThrows(IllegalStateException.class, () -> pedidoService.crearPedido(pedido));
        verify(pedidoRepository, never()).save(any());
    }

    // ─── DESPACHO: transición de estado + integración con MS-Inventario ──

    @Test
    @DisplayName("Despachar pedido debe cambiar estado a EN_CAMINO y notificar observadores")
    void despacharPedido_cambiaEstadoYNotifica() {
        Pedido pedido = new Pedido(1L, 1L, 1L, "Pudahuel", "Juan", "AB-1234");
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any())).thenReturn(pedido);
        when(centroRepository.findById(1L)).thenReturn(Optional.of(centroActivoConCapacidad()));

        Optional<Pedido> resultado = pedidoService.despacharPedido(1L);

        assertTrue(resultado.isPresent());
        assertEquals("EN_CAMINO", resultado.get().getEstado());
        verify(observadorMock).onPedidoActualizado(any(Pedido.class), eq("PENDIENTE"));
    }

    @Test
    @DisplayName("Despachar un pedido sin producto asociado no llama a MS-Inventario")
    void despacharPedido_sinProducto_noLlamaInventario() {
        Pedido pedido = new Pedido(1L, 1L, 1L, "Pudahuel", "Juan", "AB-1234");
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any())).thenReturn(pedido);

        pedidoService.despacharPedido(1L);

        verify(inventarioClient, never()).descontarStock(any(), any());
    }

    @Test
    @DisplayName("Despachar un pedido con producto asociado descuenta el stock en MS-Inventario")
    void despacharPedido_conProducto_descuentaStockEnInventario() {
        Pedido pedido = new Pedido(1L, 1L, 1L, "Pudahuel", "Juan", "AB-1234");
        pedido.setProductoId(42L);
        pedido.setCantidadProducto(3);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any())).thenReturn(pedido);
        when(inventarioClient.descontarStock(42L, 3)).thenReturn(true);

        pedidoService.despacharPedido(1L);

        verify(inventarioClient).descontarStock(42L, 3);
    }

    @Test
    @DisplayName("Despachar pedido libera el espacio reservado en el centro de distribución")
    void despacharPedido_liberaEspacioEnElCentro() {
        Pedido pedido = new Pedido(1L, 1L, 1L, "Pudahuel", "Juan", "AB-1234");
        CentroDistribucion centro = centroActivoConCapacidad();
        centro.setOcupacionActual(1);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any())).thenReturn(pedido);
        when(centroRepository.findById(1L)).thenReturn(Optional.of(centro));

        pedidoService.despacharPedido(1L);

        assertEquals(0, centro.getOcupacionActual());
    }

    @Test
    @DisplayName("Despachar un pedido que no está PENDIENTE lanza IllegalStateException")
    void despacharPedido_estadoInvalido_lanzaExcepcion() {
        Pedido pedido = new Pedido(1L, 1L, 1L, "Pudahuel", "Juan", "AB-1234");
        pedido.setEstado("EN_CAMINO");
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        assertThrows(IllegalStateException.class, () -> pedidoService.despacharPedido(1L));
        verify(pedidoRepository, never()).save(any());
        verify(observadorMock, never()).onPedidoActualizado(any(), any());
    }

    @Test
    @DisplayName("Despachar pedido inexistente debe retornar Optional vacío sin notificar")
    void despacharPedido_inexistente_noNotifica() {
        when(pedidoRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Pedido> resultado = pedidoService.despacharPedido(999L);

        assertFalse(resultado.isPresent());
        verify(observadorMock, never()).onPedidoActualizado(any(), any());
    }

    // ─── ENTREGA ──────────────────────────────────────────────────────────

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
    @DisplayName("Confirmar entrega de un pedido que no está EN_CAMINO lanza IllegalStateException")
    void confirmarEntrega_estadoInvalido_lanzaExcepcion() {
        Pedido pedido = new Pedido(1L, 1L, 1L, "Maipú", "Pedro", "CD-5678");
        // recién creado: sigue PENDIENTE, nunca fue despachado
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        assertThrows(IllegalStateException.class, () -> pedidoService.confirmarEntrega(1L, "obs"));
        verify(pedidoRepository, never()).save(any());
    }

    // ─── CANCELACIÓN ──────────────────────────────────────────────────────

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
    @DisplayName("Cancelar un pedido ya EN_CAMINO lanza IllegalStateException")
    void cancelarPedido_yaEnCamino_lanzaExcepcion() {
        Pedido pedido = new Pedido(1L, 1L, 1L, "Destino", "Resp", "ZZ-9999");
        pedido.setEstado("EN_CAMINO");
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        assertThrows(IllegalStateException.class, () -> pedidoService.cancelarPedido(1L, "motivo"));
        verify(pedidoRepository, never()).save(any());
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
