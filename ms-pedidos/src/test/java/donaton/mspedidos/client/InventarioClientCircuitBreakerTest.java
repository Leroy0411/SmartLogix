package donaton.mspedidos.client;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * PATRÓN: Circuit Breaker — mismo tipo de prueba de integración ligera que
 * {@code donaton.bff.client.InventarioClientCircuitBreakerTest} en el BFF,
 * pero aquí para la llamada de MS-Pedidos a MS-Inventario al despachar un
 * pedido: levanta el contexto real de Spring para que el proxy AOP de
 * Resilience4j intercepte la llamada, y verifica que tras acumular fallos
 * el circuito abre y deja de golpear la red (fail-fast al fallback).
 */
@SpringBootTest
@TestPropertySource(properties = {
        "resilience4j.circuitbreaker.instances.msInventario.sliding-window-size=4",
        "resilience4j.circuitbreaker.instances.msInventario.minimum-number-of-calls=4",
        "resilience4j.circuitbreaker.instances.msInventario.failure-rate-threshold=50",
        "resilience4j.circuitbreaker.instances.msInventario.wait-duration-in-open-state=5s",
        "resilience4j.circuitbreaker.instances.msInventario.permitted-number-of-calls-in-half-open-state=2"
})
@DisplayName("InventarioClient (MS-Pedidos) - Circuit Breaker (comportamiento real)")
class InventarioClientCircuitBreakerTest {

    @Autowired
    private InventarioClient inventarioClient;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @MockBean
    private RestTemplate restTemplate;

    @Test
    @DisplayName("Tras superar el umbral de fallos, el circuito ABRE y el fallback deja de golpear la red")
    void circuitoAbreYCortaLlamadasDeRed() {
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("msInventario");
        cb.transitionToClosedState();

        when(restTemplate.exchange(
                anyString(), eq(HttpMethod.PUT), any(HttpEntity.class), eq(Void.class)))
                .thenThrow(new ResourceAccessException("Connection refused"));

        for (int i = 0; i < 4; i++) {
            boolean resultado = inventarioClient.descontarStock(1L, 1);
            assertFalse(resultado);
        }
        verify(restTemplate, times(4)).exchange(
                anyString(), eq(HttpMethod.PUT), any(HttpEntity.class), eq(Void.class));

        assertEquals(CircuitBreaker.State.OPEN, cb.getState());

        boolean resultadoConCircuitoAbierto = inventarioClient.descontarStock(1L, 1);
        assertFalse(resultadoConCircuitoAbierto);

        // el circuito abierto responde fail-fast: no debe haber una 5ta llamada real
        verify(restTemplate, times(4)).exchange(
                anyString(), eq(HttpMethod.PUT), any(HttpEntity.class), eq(Void.class));
    }

    @Test
    @DisplayName("Con el circuito CLOSED y MS-Inventario disponible, el descuento se confirma")
    void circuitoCerrado_llamadaExitosa_confirmaDescuento() {
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("msInventario");
        cb.transitionToClosedState();

        when(restTemplate.exchange(
                anyString(), eq(HttpMethod.PUT), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(ResponseEntity.ok().build());

        boolean resultado = inventarioClient.descontarStock(1L, 1);

        assertTrue(resultado);
        assertEquals(CircuitBreaker.State.CLOSED, cb.getState());
    }
}
