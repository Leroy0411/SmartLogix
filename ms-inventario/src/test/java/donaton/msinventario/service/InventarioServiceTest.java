package donaton.msinventario.service;

import donaton.msinventario.factory.ProductoFactory;
import donaton.msinventario.factory.ProductoFactoryProvider;
import donaton.msinventario.model.Producto;
import donaton.msinventario.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para InventarioService.
 *
 * Se utilizan mocks de ProductoRepository y ProductoFactoryProvider
 * para aislar el comportamiento del servicio (Repository Pattern).
 * Cobertura: creación, consulta, actualización de estado, descuento de
 * stock y eliminación.
 */
@DisplayName("InventarioService - Pruebas Unitarias")
class InventarioServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private ProductoFactoryProvider factoryProvider;

    @Mock
    private ProductoFactory productoFactory;

    @InjectMocks
    private InventarioService inventarioService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ─── PRUEBAS DE CREACIÓN (Factory Method) ───────────────────────────

    @Test
    @DisplayName("Crear producto ELECTRONICA debe usar la fábrica correspondiente y guardarlo")
    void crearProducto_categoriaElectronica_usaFactoriaYGuarda() {
        Producto productoEsperado = new Producto(null, "ELECTRONICA", "Santiago", 50, 1L, "Notebooks");
        when(factoryProvider.obtenerFabrica("ELECTRONICA")).thenReturn(productoFactory);
        when(productoFactory.crear("Santiago", 50, 1L, "Notebooks")).thenReturn(productoEsperado);
        when(productoRepository.save(productoEsperado)).thenReturn(productoEsperado);

        Producto resultado = inventarioService.crearProducto("ELECTRONICA", "Santiago", 50, 1L, "Notebooks");

        assertNotNull(resultado);
        assertEquals("ELECTRONICA", resultado.getCategoria());
        verify(factoryProvider).obtenerFabrica("ELECTRONICA");
        verify(productoFactory).crear("Santiago", 50, 1L, "Notebooks");
        verify(productoRepository).save(productoEsperado);
    }

    @Test
    @DisplayName("Crear producto con categoría inválida debe propagar IllegalArgumentException")
    void crearProducto_categoriaInvalida_lanzaExcepcion() {
        when(factoryProvider.obtenerFabrica("INVALIDA"))
                .thenThrow(new IllegalArgumentException("Categoría no soportada: INVALIDA"));

        assertThrows(IllegalArgumentException.class,
                () -> inventarioService.crearProducto("INVALIDA", "Proveedor", 10, 1L, "desc"));
    }

    // ─── PRUEBAS DE CONSULTA (Repository Pattern) ───────────────────────

    @Test
    @DisplayName("ObtenerTodos debe retornar la lista completa del repositorio")
    void obtenerTodos_retornaListaCompleta() {
        List<Producto> listaMock = List.of(
                new Producto(1L, "ELECTRONICA", "A", 10, 1L, "desc1"),
                new Producto(2L, "HOGAR", "B", 5, 2L, "desc2")
        );
        when(productoRepository.findAll()).thenReturn(listaMock);

        List<Producto> resultado = inventarioService.obtenerTodos();

        assertEquals(2, resultado.size());
        verify(productoRepository).findAll();
    }

    @Test
    @DisplayName("ObtenerPorId con ID existente debe retornar Optional con producto")
    void obtenerPorId_existente_retornaOptionalPresente() {
        Producto producto = new Producto(1L, "ALIMENTOS", "Maipú", 30, 1L, "Conservas");
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        Optional<Producto> resultado = inventarioService.obtenerPorId(1L);

        assertTrue(resultado.isPresent());
        assertEquals("ALIMENTOS", resultado.get().getCategoria());
    }

    @Test
    @DisplayName("ObtenerPorId con ID inexistente debe retornar Optional vacío")
    void obtenerPorId_inexistente_retornaOptionalVacio() {
        when(productoRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Producto> resultado = inventarioService.obtenerPorId(999L);

        assertFalse(resultado.isPresent());
    }

    @Test
    @DisplayName("ObtenerPorEstado debe filtrar correctamente usando el repositorio")
    void obtenerPorEstado_delegaAlRepositorio() {
        List<Producto> disponibles = List.of(
                new Producto(1L, "ELECTRONICA", "A", 10, 1L, "d")
        );
        when(productoRepository.findByEstado("DISPONIBLE")).thenReturn(disponibles);

        List<Producto> resultado = inventarioService.obtenerPorEstado("DISPONIBLE");

        assertEquals(1, resultado.size());
        verify(productoRepository).findByEstado("DISPONIBLE");
    }

    // ─── PRUEBAS DE ACTUALIZACIÓN ────────────────────────────────────────

    @Test
    @DisplayName("ActualizarEstado con ID existente debe cambiar el estado y persistir")
    void actualizarEstado_existente_actualizaYGuarda() {
        Producto producto = new Producto(1L, "ELECTRONICA", "A", 10, 1L, "d");
        producto.setEstado("DISPONIBLE");
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(productoRepository.save(any())).thenReturn(producto);

        Optional<Producto> resultado = inventarioService.actualizarEstado(1L, "RESERVADO");

        assertTrue(resultado.isPresent());
        assertEquals("RESERVADO", resultado.get().getEstado());
        verify(productoRepository).save(producto);
    }

    @Test
    @DisplayName("ActualizarEstado con ID inexistente debe retornar Optional vacío")
    void actualizarEstado_inexistente_retornaVacio() {
        when(productoRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Producto> resultado = inventarioService.actualizarEstado(999L, "RESERVADO");

        assertFalse(resultado.isPresent());
        verify(productoRepository, never()).save(any());
    }

    @Test
    @DisplayName("DescontarStock reduce el stock y marca AGOTADO al llegar a cero")
    void descontarStock_llegaACero_marcaAgotado() {
        Producto producto = new Producto(1L, "ELECTRONICA", "A", 5, 1L, "d");
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(productoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Optional<Producto> resultado = inventarioService.descontarStock(1L, 5);

        assertTrue(resultado.isPresent());
        assertEquals(0, resultado.get().getStock());
        assertEquals("AGOTADO", resultado.get().getEstado());
    }

    @Test
    @DisplayName("DescontarStock parcial mantiene el estado actual")
    void descontarStock_parcial_mantieneEstado() {
        Producto producto = new Producto(1L, "ELECTRONICA", "A", 10, 1L, "d");
        producto.setEstado("DISPONIBLE");
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(productoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Optional<Producto> resultado = inventarioService.descontarStock(1L, 4);

        assertTrue(resultado.isPresent());
        assertEquals(6, resultado.get().getStock());
        assertEquals("DISPONIBLE", resultado.get().getEstado());
    }

    // ─── PRUEBAS DE ELIMINACIÓN ──────────────────────────────────────────

    @Test
    @DisplayName("Eliminar con ID existente debe retornar true")
    void eliminar_existente_retornaTrue() {
        when(productoRepository.existsById(1L)).thenReturn(true);

        boolean resultado = inventarioService.eliminar(1L);

        assertTrue(resultado);
        verify(productoRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Eliminar con ID inexistente debe retornar false")
    void eliminar_inexistente_retornaFalse() {
        when(productoRepository.existsById(999L)).thenReturn(false);

        boolean resultado = inventarioService.eliminar(999L);

        assertFalse(resultado);
        verify(productoRepository, never()).deleteById(any());
    }
}
