package donaton.mspedidos.repository;

import donaton.mspedidos.model.CentroDistribucion;
import donaton.mspedidos.model.Pedido;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integración para los repositorios de MS-Pedidos usando @DataJpaTest.
 * Levanta una base H2 en memoria para validar la persistencia real mediante
 * JPA/Hibernate tanto de centros de distribución como de pedidos.
 */
@DataJpaTest
@DisplayName("Repositorios de Pedidos (JPA) - Pruebas de Integración")
class PedidoRepositoryTest {

    @Autowired
    private CentroDistribucionRepository centroRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Test
    @DisplayName("CentroDistribucionRepository: save asigna ID autogenerado")
    void centroRepository_save_asignaId() {
        CentroDistribucion centro = new CentroDistribucion(null, "Centro Test", "Dir 123", "Maipú",
                100, "Resp", "contacto@test.cl");
        CentroDistribucion guardado = centroRepository.save(centro);

        assertNotNull(guardado.getId());
        assertTrue(centroRepository.findById(guardado.getId()).isPresent());
    }

    @Test
    @DisplayName("CentroDistribucionRepository: findByEstado filtra correctamente")
    void centroRepository_findByEstado_filtraCorrectamente() {
        CentroDistribucion activo = new CentroDistribucion(null, "A", "Dir", "Maipú", 100, "R", "c");
        CentroDistribucion saturado = new CentroDistribucion(null, "B", "Dir", "Maipú", 50, "R", "c");
        saturado.setEstado("SATURADO");
        centroRepository.save(activo);
        centroRepository.save(saturado);

        List<CentroDistribucion> activos = centroRepository.findByEstado("ACTIVO");
        assertEquals(1, activos.size());
        assertEquals("A", activos.get(0).getNombre());
    }

    @Test
    @DisplayName("PedidoRepository: save asigna ID autogenerado")
    void pedidoRepository_save_asignaId() {
        Pedido pedido = new Pedido(null, 1L, 1L, "Pudahuel", "Juan", "AB-1234");
        Pedido guardado = pedidoRepository.save(pedido);

        assertNotNull(guardado.getId());
        assertTrue(pedidoRepository.findById(guardado.getId()).isPresent());
    }

    @Test
    @DisplayName("PedidoRepository: findByEstado filtra correctamente")
    void pedidoRepository_findByEstado_filtraCorrectamente() {
        Pedido pendiente = new Pedido(null, 1L, 1L, "Destino A", "Juan", "AB-1234");
        Pedido enCamino = new Pedido(null, 1L, 2L, "Destino B", "Pedro", "CD-5678");
        enCamino.marcarDespachado();
        pedidoRepository.save(pendiente);
        pedidoRepository.save(enCamino);

        List<Pedido> pendientes = pedidoRepository.findByEstado("PENDIENTE");
        assertEquals(1, pendientes.size());
        assertEquals("Destino A", pendientes.get(0).getDireccionEntrega());
    }

    @Test
    @DisplayName("PedidoRepository: deleteById elimina el registro persistido")
    void pedidoRepository_deleteById_elimina() {
        Pedido pedido = pedidoRepository.save(new Pedido(null, 1L, 1L, "Destino", "Resp", "ZZ-9999"));
        pedidoRepository.deleteById(pedido.getId());

        assertTrue(pedidoRepository.findById(pedido.getId()).isEmpty());
    }
}
