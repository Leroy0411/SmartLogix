package donaton.mspedidos.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Pruebas unitarias del ApiKeyFilter de MS-Pedidos (mismo comportamiento
 * que el de MS-Inventario, ver esa clase para el detalle de diseño).
 */
@DisplayName("ApiKeyFilter (MS-Pedidos) - Pruebas Unitarias")
class ApiKeyFilterTest {

    private static final String CLAVE_VALIDA = "clave-de-prueba";

    private ApiKeyFilter filtro;

    @BeforeEach
    void setUp() {
        filtro = new ApiKeyFilter();
        ReflectionTestUtils.setField(filtro, "apiKeyEsperada", CLAVE_VALIDA);
    }

    @Test
    @DisplayName("PUT sin header X-API-KEY es rechazado con 401")
    void escrituraSinApiKey_retorna401() throws IOException, jakarta.servlet.ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest("PUT", "/api/pedidos/1/despachar");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filtro.doFilter(request, response, chain);

        assertEquals(401, response.getStatus());
        verifyNoInteractions(chain);
    }

    @Test
    @DisplayName("PUT con X-API-KEY correcta continúa la cadena de filtros")
    void escrituraConApiKeyValida_continua() throws IOException, jakarta.servlet.ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest("PUT", "/api/pedidos/1/despachar");
        request.addHeader("X-API-KEY", CLAVE_VALIDA);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filtro.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    @DisplayName("GET (lectura) nunca exige X-API-KEY")
    void lectura_noExigeApiKey() throws IOException, jakarta.servlet.ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/pedidos");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filtro.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }
}
