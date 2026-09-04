package donaton.msinventario.factory;

import donaton.msinventario.model.Producto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para las fábricas concretas (Factory Method).
 * Verifica las reglas de negocio específicas de cada categoría de producto
 * y la validación común definida en ProductoFactory.
 */
@DisplayName("Fábricas de Producto - Pruebas Unitarias")
class ProductoFactoriesImplTest {

    @Test
    @DisplayName("ProductoElectronicaFactory: lote >= 100 unidades pasa directo a RESERVADO")
    void electronicaFactory_loteGrande_pasaAReservado() {
        ProductoFactory fabrica = new ProductoElectronicaFactory();
        Producto p = fabrica.crear("Santiago", 150, 1L, "Lote grande de electrónica");

        assertEquals("ELECTRONICA", p.getCategoria());
        assertEquals("RESERVADO", p.getEstado());
    }

    @Test
    @DisplayName("ProductoElectronicaFactory: lote pequeño se mantiene DISPONIBLE")
    void electronicaFactory_loteChico_mantieneDisponible() {
        ProductoFactory fabrica = new ProductoElectronicaFactory();
        Producto p = fabrica.crear("Santiago", 5, 1L, "Lote chico");

        assertEquals("DISPONIBLE", p.getEstado());
    }

    @Test
    @DisplayName("ProductoAlimentosFactory: siempre inicia DISPONIBLE")
    void alimentosFactory_siempreDisponible() {
        ProductoFactory fabrica = new ProductoAlimentosFactory();
        Producto p = fabrica.crear("Maipú", 30, 2L, "Conservas");

        assertEquals("ALIMENTOS", p.getCategoria());
        assertEquals("DISPONIBLE", p.getEstado());
    }

    @Test
    @DisplayName("ProductoHogarFactory: siempre pasa a RESERVADO por alta rotación")
    void hogarFactory_altaRotacion_pasaAReservado() {
        ProductoFactory fabrica = new ProductoHogarFactory();
        Producto p = fabrica.crear("Ñuñoa", 10, 1L, "Artículos de hogar");

        assertEquals("HOGAR", p.getCategoria());
        assertEquals("RESERVADO", p.getEstado());
    }

    @Test
    @DisplayName("ProductoRopaFactory: inicia DISPONIBLE")
    void ropaFactory_iniciaDisponible() {
        ProductoFactory fabrica = new ProductoRopaFactory();
        Producto p = fabrica.crear("Maipú", 15, 1L, "Vestuario");

        assertEquals("ROPA", p.getCategoria());
        assertEquals("DISPONIBLE", p.getEstado());
    }

    @Test
    @DisplayName("getCategoria() retorna el identificador correcto de cada fábrica")
    void getCategoria_retornaIdentificadorCorrecto() {
        assertEquals("ELECTRONICA", new ProductoElectronicaFactory().getCategoria());
        assertEquals("ALIMENTOS", new ProductoAlimentosFactory().getCategoria());
        assertEquals("HOGAR", new ProductoHogarFactory().getCategoria());
        assertEquals("ROPA", new ProductoRopaFactory().getCategoria());
    }

    @Test
    @DisplayName("crear() con proveedor vacío lanza IllegalArgumentException")
    void crear_proveedorVacio_lanzaExcepcion() {
        ProductoFactory fabrica = new ProductoElectronicaFactory();
        assertThrows(IllegalArgumentException.class,
                () -> fabrica.crear("", 10, 1L, "desc"));
    }

    @Test
    @DisplayName("crear() con proveedor nulo lanza IllegalArgumentException")
    void crear_proveedorNulo_lanzaExcepcion() {
        ProductoFactory fabrica = new ProductoAlimentosFactory();
        assertThrows(IllegalArgumentException.class,
                () -> fabrica.crear(null, 10, 1L, "desc"));
    }

    @Test
    @DisplayName("crear() con stock cero o negativo lanza IllegalArgumentException")
    void crear_stockInvalido_lanzaExcepcion() {
        ProductoFactory fabrica = new ProductoHogarFactory();
        assertThrows(IllegalArgumentException.class,
                () -> fabrica.crear("Proveedor", 0, 1L, "desc"));
        assertThrows(IllegalArgumentException.class,
                () -> fabrica.crear("Proveedor", -5, 1L, "desc"));
    }
}
