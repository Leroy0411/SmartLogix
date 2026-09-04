package donaton.bff.controller;

import donaton.bff.dto.DashboardResumenDTO;
import donaton.bff.service.BffService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas unitarias del BffController mediante @WebMvcTest.
 * Verifica que el contrato HTTP de los endpoints agregados del BFF
 * sea correcto, sin levantar el contexto completo de Spring.
 */
@WebMvcTest(BffController.class)
@DisplayName("BffController - Pruebas Unitarias")
class BffControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BffService bffService;

    @Test
    @DisplayName("GET /bff/dashboard retorna 200 con el resumen agregado")
    void getDashboard_retornaOk() throws Exception {
        DashboardResumenDTO resumen = new DashboardResumenDTO();
        resumen.setTotalProductos(5);
        resumen.setTotalCentrosDistribucion(2);
        resumen.setTotalPedidos(3);
        resumen.setAlertas(List.of());

        when(bffService.obtenerResumenDashboard()).thenReturn(resumen);

        mockMvc.perform(get("/bff/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProductos").value(5))
                .andExpect(jsonPath("$.totalCentrosDistribucion").value(2))
                .andExpect(jsonPath("$.totalPedidos").value(3));
    }

    @Test
    @DisplayName("GET /bff/health retorna 200 con mensaje de estado OK")
    void health_retornaOk() throws Exception {
        mockMvc.perform(get("/bff/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("BFF-Donaton OK"));
    }
}
