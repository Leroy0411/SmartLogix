package donaton.msinventario.repository;

import donaton.msinventario.model.Producto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integración para ProductoRepository usando @DataJpaTest.
 * Levanta una base H2 en memoria (autoconfigurada por Spring Boot Test)
 * para validar que la persistencia real mediante JPA/Hibernate funciona
 * correctamente: guardado, búsqueda por id, por estado, por bodega y borrado.
 */
@DataJpaTest
@DisplayName("ProductoRepository (JPA) - Pruebas de Integración")
class ProductoRepositoryTest {

    @Autowired
    private ProductoRepository repository;

    @Test
    @DisplayName("Save persiste el producto y le asigna un ID autogenerado")
    void save_productoNuevo_asignaId() {
        Producto p = new Producto(null, "ELECTRONICA", "Proveedor", 10, 1L, "desc");
        Producto guardado = repository.save(p);

        assertNotNull(guardado.getId());
        assertTrue(repository.findById(guardado.getId()).isPresent());
    }

    @Test
    @DisplayName("FindAll retorna todos los productos persistidos")
    void findAll_variosGuardados_retornaTodosCorrectamente() {
        repository.save(new Producto(null, "ELECTRONICA", "A", 10, 1L, "p1"));
        repository.save(new Producto(null, "HOGAR", "B", 5, 1L, "p2"));

        List<Producto> resultado = repository.findAll();
        assertEquals(2, resultado.size());
    }

    @Test
    @DisplayName("FindById con ID inexistente retorna Optional vacío")
    void findById_inexistente_retornaVacio() {
        Optional<Producto> resultado = repository.findById(999L);
        assertFalse(resultado.isPresent());
    }

    @Test
    @DisplayName("FindByEstado filtra correctamente por estado persistido")
    void findByEstado_filtraCorrectamente() {
        Producto p1 = repository.save(new Producto(null, "ELECTRONICA", "A", 10, 1L, "p1"));
        Producto p2 = repository.save(new Producto(null, "HOGAR", "B", 5, 1L, "p2"));
        p2.setEstado("RESERVADO");
        repository.save(p2);

        List<Producto> disponibles = repository.findByEstado("DISPONIBLE");
        assertEquals(1, disponibles.size());
        assertEquals(p1.getId(), disponibles.get(0).getId());
    }

    @Test
    @DisplayName("FindByBodegaId filtra correctamente por bodega")
    void findByBodegaId_filtraCorrectamente() {
        repository.save(new Producto(null, "ELECTRONICA", "A", 10, 1L, "p1"));
        repository.save(new Producto(null, "HOGAR", "B", 5, 2L, "p2"));

        List<Producto> resultado = repository.findByBodegaId(1L);
        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getBodegaId());
    }

    @Test
    @DisplayName("DeleteById elimina el producto de la base de datos")
    void deleteById_existente_elimina() {
        Producto guardado = repository.save(new Producto(null, "ROPA", "A", 5, 1L, "p"));
        repository.deleteById(guardado.getId());

        assertTrue(repository.findById(guardado.getId()).isEmpty());
    }

    @Test
    @DisplayName("ExistsById refleja correctamente la presencia del registro")
    void existsById_existenteEInexistente() {
        Producto guardado = repository.save(new Producto(null, "ALIMENTOS", "X", 20, 2L, "p"));

        assertTrue(repository.existsById(guardado.getId()));
        assertFalse(repository.existsById(999L));
    }
}
