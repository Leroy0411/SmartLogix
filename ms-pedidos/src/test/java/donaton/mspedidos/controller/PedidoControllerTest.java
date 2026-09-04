package donaton.mspedidos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import donaton.mspedidos.model.CentroDistribucion;
import donaton.mspedidos.model.Pedido;
import donaton.mspedidos.service.PedidoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas unitarias del PedidoController mediante @WebMvcTest.
 * Verifica el contrato HTTP de los endpoints de centros de distribución y pedidos.
 */
@WebMvcTest(PedidoController.class)
@DisplayName("PedidoController - Pruebas Unitarias")
class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PedidoService pedidoService;

    @Autowired
    private ObjectMapper objectMapper;

    private CentroDistribucion centroEjemplo;
    private Pedido pedidoEjemplo;

    @BeforeEach
    void setUp() {
        centroEjemplo = new CentroDistribucion(1L, "Centro A", "Dir 123", "Maipú", 100, "Resp", "contacto");
        pedidoEjemplo = new Pedido(1L, 1L, 1L, "Pudahuel", "Juan", "AB-1234");
    }

    @Test
    @DisplayName("GET /api/pedidos/centros-distribucion retorna 200 con la lista completa")
    void listarCentros_retornaOk() throws Exception {
        when(pedidoService.obtenerCentros()).thenReturn(List.of(centroEjemplo));

        mockMvc.perform(get("/api/pedidos/centros-distribucion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Centro A"));
    }

    @Test
    @DisplayName("GET /api/pedidos/centros-distribucion/activos retorna 200")
    void listarActivos_retornaOk() throws Exception {
        when(pedidoService.obtenerCentrosActivos()).thenReturn(List.of(centroEjemplo));

        mockMvc.perform(get("/api/pedidos/centros-distribucion/activos"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/pedidos/centros-distribucion retorna 201 con el centro creado")
    void crearCentro_retornaCreated() throws Exception {
        when(pedidoService.agregarCentro(any())).thenReturn(centroEjemplo);

        mockMvc.perform(post("/api/pedidos/centros-distribucion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(centroEjemplo)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Centro A"));
    }

    @Test
    @DisplayName("PUT /api/pedidos/centros-distribucion/{id}/ocupacion con ID existente retorna 200")
    void actualizarOcupacion_existente_retornaOk() throws Exception {
        when(pedidoService.actualizarOcupacion(1L, 50)).thenReturn(Optional.of(centroEjemplo));

        mockMvc.perform(put("/api/pedidos/centros-distribucion/1/ocupacion").param("ocupacion", "50"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/pedidos/centros-distribucion/{id}/ocupacion con ID inexistente retorna 404")
    void actualizarOcupacion_inexistente_retornaNotFound() throws Exception {
        when(pedidoService.actualizarOcupacion(999L, 50)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/pedidos/centros-distribucion/999/ocupacion").param("ocupacion", "50"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/pedidos/centros-distribucion/{id} existente retorna 200")
    void eliminarCentro_existente_retornaOk() throws Exception {
        when(pedidoService.eliminarCentro(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/pedidos/centros-distribucion/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    @DisplayName("GET /api/pedidos retorna 200 con la lista completa")
    void listarPedidos_retornaOk() throws Exception {
        when(pedidoService.obtenerPedidos()).thenReturn(List.of(pedidoEjemplo));

        mockMvc.perform(get("/api/pedidos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].direccionEntrega").value("Pudahuel"));
    }

    @Test
    @DisplayName("POST /api/pedidos retorna 201 con el pedido creado")
    void crearPedido_retornaCreated() throws Exception {
        when(pedidoService.crearPedido(any())).thenReturn(pedidoEjemplo);

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pedidoEjemplo)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("PUT /api/pedidos/{id}/despachar con ID existente retorna 200")
    void despachar_existente_retornaOk() throws Exception {
        when(pedidoService.despacharPedido(1L)).thenReturn(Optional.of(pedidoEjemplo));

        mockMvc.perform(put("/api/pedidos/1/despachar"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/pedidos/{id}/despachar con ID inexistente retorna 404")
    void despachar_inexistente_retornaNotFound() throws Exception {
        when(pedidoService.despacharPedido(999L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/pedidos/999/despachar"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/pedidos/{id}/entregar retorna 200")
    void entregar_existente_retornaOk() throws Exception {
        when(pedidoService.confirmarEntrega(1L, "Todo OK")).thenReturn(Optional.of(pedidoEjemplo));

        mockMvc.perform(put("/api/pedidos/1/entregar").param("observaciones", "Todo OK"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/pedidos/{id}/cancelar retorna 200")
    void cancelar_existente_retornaOk() throws Exception {
        when(pedidoService.cancelarPedido(1L, "Sin transporte")).thenReturn(Optional.of(pedidoEjemplo));

        mockMvc.perform(put("/api/pedidos/1/cancelar").param("motivo", "Sin transporte"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/pedidos/{id} inexistente retorna 404")
    void eliminarPedido_inexistente_retornaNotFound() throws Exception {
        when(pedidoService.eliminarPedido(999L)).thenReturn(false);

        mockMvc.perform(delete("/api/pedidos/999"))
                .andExpect(status().isNotFound());
    }
}
