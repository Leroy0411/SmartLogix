package donaton.msinventario.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import donaton.msinventario.model.Producto;
import donaton.msinventario.service.InventarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas unitarias del InventarioController mediante @WebMvcTest.
 * Verifica el contrato HTTP (status codes, payloads) sin levantar
 * el contexto completo de Spring ni la base de datos.
 */
@WebMvcTest(InventarioController.class)
@DisplayName("InventarioController - Pruebas Unitarias")
class InventarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventarioService inventarioService;

    @Autowired
    private ObjectMapper objectMapper;

    private Producto productoEjemplo;

    @BeforeEach
    void setUp() {
        productoEjemplo = new Producto(1L, "ELECTRONICA", "Santiago", 20, 1L, "Notebooks");
    }

    @Test
    @DisplayName("GET /api/inventario retorna 200 con la lista completa")
    void listar_retornaOk() throws Exception {
        when(inventarioService.obtenerTodos()).thenReturn(List.of(productoEjemplo));

        mockMvc.perform(get("/api/inventario"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoria").value("ELECTRONICA"));
    }

    @Test
    @DisplayName("GET /api/inventario/{id} con ID existente retorna 200")
    void obtenerPorId_existente_retornaOk() throws Exception {
        when(inventarioService.obtenerPorId(1L)).thenReturn(Optional.of(productoEjemplo));

        mockMvc.perform(get("/api/inventario/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("GET /api/inventario/{id} con ID inexistente retorna 404")
    void obtenerPorId_inexistente_retornaNotFound() throws Exception {
        when(inventarioService.obtenerPorId(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/inventario/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/inventario con datos válidos retorna 201")
    void crearProducto_valido_retornaCreated() throws Exception {
        when(inventarioService.crearProducto(any(), any(), any(), any(), any()))
                .thenReturn(productoEjemplo);

        Map<String, Object> body = Map.of(
                "categoria", "ELECTRONICA", "proveedor", "Santiago",
                "stock", 20, "bodegaId", 1, "descripcion", "Notebooks"
        );

        mockMvc.perform(post("/api/inventario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.categoria").value("ELECTRONICA"));
    }

    @Test
    @DisplayName("POST /api/inventario con categoría inválida retorna 400")
    void crearProducto_categoriaInvalida_retornaBadRequest() throws Exception {
        when(inventarioService.crearProducto(any(), any(), any(), any(), any()))
                .thenThrow(new IllegalArgumentException("Categoría de producto no soportada: XYZ"));

        Map<String, Object> body = Map.of(
                "categoria", "XYZ", "proveedor", "Santiago",
                "stock", 20, "bodegaId", 1
        );

        mockMvc.perform(post("/api/inventario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("PUT /api/inventario/{id}/estado con ID existente retorna 200")
    void actualizarEstado_existente_retornaOk() throws Exception {
        productoEjemplo.setEstado("RESERVADO");
        when(inventarioService.actualizarEstado(1L, "RESERVADO")).thenReturn(Optional.of(productoEjemplo));

        mockMvc.perform(put("/api/inventario/1/estado").param("estado", "RESERVADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("RESERVADO"));
    }

    @Test
    @DisplayName("PUT /api/inventario/{id}/estado con ID inexistente retorna 404")
    void actualizarEstado_inexistente_retornaNotFound() throws Exception {
        when(inventarioService.actualizarEstado(999L, "RESERVADO")).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/inventario/999/estado").param("estado", "RESERVADO"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/inventario/{id}/descontar-stock con ID existente retorna 200")
    void descontarStock_existente_retornaOk() throws Exception {
        productoEjemplo.setStock(15);
        when(inventarioService.descontarStock(1L, 5)).thenReturn(Optional.of(productoEjemplo));

        mockMvc.perform(put("/api/inventario/1/descontar-stock").param("cantidad", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(15));
    }

    @Test
    @DisplayName("DELETE /api/inventario/{id} existente retorna 200 con mensaje")
    void eliminar_existente_retornaOk() throws Exception {
        when(inventarioService.eliminar(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/inventario/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    @DisplayName("DELETE /api/inventario/{id} inexistente retorna 404")
    void eliminar_inexistente_retornaNotFound() throws Exception {
        when(inventarioService.eliminar(999L)).thenReturn(false);

        mockMvc.perform(delete("/api/inventario/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/inventario/estado/{estado} retorna lista filtrada")
    void listarPorEstado_retornaOk() throws Exception {
        when(inventarioService.obtenerPorEstado("DISPONIBLE")).thenReturn(List.of(productoEjemplo));

        mockMvc.perform(get("/api/inventario/estado/DISPONIBLE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("GET /api/inventario/bodega/{id} retorna lista filtrada por bodega")
    void listarPorBodega_retornaOk() throws Exception {
        when(inventarioService.obtenerPorBodega(1L)).thenReturn(List.of(productoEjemplo));

        mockMvc.perform(get("/api/inventario/bodega/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}
