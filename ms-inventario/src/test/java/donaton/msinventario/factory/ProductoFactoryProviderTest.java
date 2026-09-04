package donaton.msinventario.factory;

import donaton.msinventario.model.Producto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para ProductoFactoryProvider.
 * Verifica el registro automático de fábricas y la resolución
 * por categoría (incluyendo manejo de categorías no soportadas).
 */
@DisplayName("ProductoFactoryProvider - Pruebas Unitarias")
class ProductoFactoryProviderTest {

    private ProductoFactoryProvider provider;

    @BeforeEach
    void setUp() {
        provider = new ProductoFactoryProvider(List.of(
                new ProductoElectronicaFactory(),
                new ProductoAlimentosFactory(),
                new ProductoHogarFactory(),
                new ProductoRopaFactory()
        ));
    }

    @Test
    @DisplayName("obtenerFabrica devuelve la fábrica correcta para cada categoría soportada")
    void obtenerFabrica_categoriasValidas_retornaFabricaCorrecta() {
        assertEquals("ELECTRONICA", provider.obtenerFabrica("ELECTRONICA").getCategoria());
        assertEquals("ALIMENTOS", provider.obtenerFabrica("ALIMENTOS").getCategoria());
        assertEquals("HOGAR", provider.obtenerFabrica("HOGAR").getCategoria());
        assertEquals("ROPA", provider.obtenerFabrica("ROPA").getCategoria());
    }

    @Test
    @DisplayName("obtenerFabrica es insensible a mayúsculas/minúsculas")
    void obtenerFabrica_minusculas_resuelveCorrectamente() {
        assertEquals("ELECTRONICA", provider.obtenerFabrica("electronica").getCategoria());
    }

    @Test
    @DisplayName("obtenerFabrica con categoría no soportada lanza IllegalArgumentException")
    void obtenerFabrica_categoriaInvalida_lanzaExcepcion() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> provider.obtenerFabrica("JUGUETES"));
        assertTrue(ex.getMessage().contains("JUGUETES"));
    }

    @Test
    @DisplayName("La fábrica resuelta efectivamente crea productos de la categoría esperada")
    void fabricaResuelta_creaProductoDeLaCategoriaCorrecta() {
        Producto p = provider.obtenerFabrica("ALIMENTOS").crear("Proveedor", 10, 1L, "desc");
        assertEquals("ALIMENTOS", p.getCategoria());
    }
}
