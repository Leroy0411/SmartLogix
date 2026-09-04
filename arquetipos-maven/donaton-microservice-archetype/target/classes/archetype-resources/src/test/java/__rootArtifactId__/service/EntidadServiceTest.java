package ${package}.service;

import ${package}.model.Entidad;
import ${package}.repository.EntidadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias base generadas por el arquetipo Donaton.
 * Extender con casos de prueba específicos del dominio.
 */
@DisplayName("EntidadService - Pruebas Unitarias")
class EntidadServiceTest {

    @Mock
    private EntidadRepository repository;

    @InjectMocks
    private EntidadService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Crear entidad debe persistir mediante el repositorio")
    void crear_persisteEntidad() {
        Entidad e = new Entidad(null, "Test");
        when(repository.save(any())).thenAnswer(inv -> {
            Entidad ent = inv.getArgument(0);
            ent.setId(1L);
            return ent;
        });
        Entidad resultado = service.crear(e);
        assertNotNull(resultado.getId());
        verify(repository).save(e);
    }

    @Test
    @DisplayName("ObtenerTodas delega al repositorio")
    void obtenerTodas_delegaAlRepositorio() {
        when(repository.findAll()).thenReturn(List.of(new Entidad(1L, "A")));
        List<Entidad> resultado = service.obtenerTodas();
        assertEquals(1, resultado.size());
    }

    @Test
    @DisplayName("ObtenerPorId existente retorna Optional presente")
    void obtenerPorId_existente_retornaPresente() {
        when(repository.findById(1L)).thenReturn(Optional.of(new Entidad(1L, "A")));
        assertTrue(service.obtenerPorId(1L).isPresent());
    }

    @Test
    @DisplayName("Eliminar existente retorna true")
    void eliminar_existente_retornaTrue() {
        when(repository.deleteById(1L)).thenReturn(true);
        assertTrue(service.eliminar(1L));
    }
}
