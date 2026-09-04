package donaton.bff.client;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * PATRÓN: Circuit Breaker — pruebas de integración ligera.
 * ─────────────────────────────────────────────────────────────────────────
 * A diferencia de BffServiceTest (que mockea directamente el resultado del
 * cliente), esta prueba levanta el contexto de Spring real para que el
 * proxy AOP de Resilience4j intercepte las llamadas a InventarioClient,
 * y verifica el comportamiento efectivo del patrón:
 *
 *  1. Con el circuito CLOSED, cada llamada fallida SÍ golpea la red
 *     (RestTemplate) y devuelve el fallback.
 *  2. Tras acumular fallos suficientes para superar el umbral configurado,
 *     el circuito pasa a OPEN.
 *  3. Con el circuito OPEN, las llamadas siguientes van directo al
 *     fallback SIN invocar RestTemplate (fail-fast) — se verifica contando
 *     las invocaciones reales a restTemplate.exchange(...).
 *
 * Los umbrales se sobrescriben a valores bajos solo para este test, para no
 * depender de docenas de llamadas ni de tiempos de espera largos.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "resilience4j.circuitbreaker.instances.msInventario.sliding-window-size=4",
        "resilience4j.circuitbreaker.instances.msInventario.minimum-number-of-calls=4",
        "resilience4j.circuitbreaker.instances.msInventario.failure-rate-threshold=50",
        "resilience4j.circuitbreaker.instances.msInventario.wait-duration-in-open-state=5s",
        "resilience4j.circuitbreaker.instances.msInventario.permitted-number-of-calls-in-half-open-state=2"
})
@DisplayName("InventarioClient - Circuit Breaker (comportamiento real)")
class InventarioClientCircuitBreakerTest {

    @Autowired
    private InventarioClient inventarioClient;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @MockBean
    private RestTemplate restTemplate;

    @Test
    @DisplayName("Tras superar el umbral de fallos, el circuito ABRE y deja de llamar a MS-Inventario")
    @SuppressWarnings("unchecked")
    void circuitoAbreYCortaLlamadasDeRed() {
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("msInventario");
        cb.transitionToClosedState(); // estado limpio, por si otro test ya lo abrió

        when(restTemplate.exchange(
                anyString(), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenThrow(new ResourceAccessException("Connection refused"));

        // minimum-number-of-calls=4 → estas 4 llamadas SÍ deben ir a la red
        for (int i = 0; i < 4; i++) {
            ServicioResultado<?> resultado = inventarioClient.obtenerProductos();
            assertFalse(resultado.isDisponible());
        }
        verify(restTemplate, times(4)).exchange(
                anyString(), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class));

        // Con 100% de fallos en la ventana, el circuito debe estar OPEN
        assertEquals(CircuitBreaker.State.OPEN, cb.getState());

        // Nuevas llamadas deben resolverse por fallback SIN tocar la red
        ServicioResultado<?> resultadoConCircuitoAbierto = inventarioClient.obtenerProductos();
        assertFalse(resultadoConCircuitoAbierto.isDisponible());
        assertTrue(resultadoConCircuitoAbierto.getMensajeError().contains("MS-Inventario"));

        // El contador de invocaciones reales a RestTemplate NO debe haber aumentado
        verify(restTemplate, times(4)).exchange(
                anyString(), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class));
    }

    @Test
    @DisplayName("Con el circuito CLOSED y el servicio disponible, los datos llegan normalmente")
    @SuppressWarnings("unchecked")
    void circuitoCerrado_llamadaExitosa_devuelveDatos() {
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("msInventario");
        cb.transitionToClosedState();

        when(restTemplate.exchange(
                anyString(), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(org.springframework.http.ResponseEntity.ok(java.util.List.of()));

        ServicioResultado<?> resultado = inventarioClient.obtenerProductos();

        assertTrue(resultado.isDisponible());
        assertEquals(CircuitBreaker.State.CLOSED, cb.getState());
    }
}
