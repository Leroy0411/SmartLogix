package donaton.bff.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * PATRÓN: Circuit Breaker (Resilience4j)
 * ─────────────────────────────────────────────────────────────────────────
 * Encapsula la comunicación HTTP del BFF con MS-Inventario (puerto 8081).
 *
 * El circuito "msInventario" monitorea la tasa de fallos/lentitud de las
 * llamadas. Mientras está CLOSED, las peticiones fluyen normalmente. Si el
 * porcentaje de fallos supera el umbral configurado (application.properties),
 * el circuito pasa a OPEN: durante ese período todas las llamadas se
 * redirigen instantáneamente al método de fallback, SIN intentar la
 * conexión de red, evitando que la caída de MS-Inventario deje al BFF (y
 * transitivamente al frontend) esperando timeouts en cascada.
 * Pasado `wait-duration-in-open-state`, el circuito prueba en HALF_OPEN si
 * el servicio ya se recuperó antes de volver a CLOSED.
 *
 * Nota técnica: la anotación @CircuitBreaker solo intercepta llamadas que
 * pasan por el proxy de Spring (por eso vive en su propio bean, separado
 * de BffService que lo consume).
 */
@Component
public class InventarioClient {

    private final RestTemplate restTemplate;

    @Value("${ms.inventario.url:http://localhost:8081}")
    private String urlInventario;

    public InventarioClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @CircuitBreaker(name = "msInventario", fallbackMethod = "fallbackObtenerProductos")
    public ServicioResultado<List<Map<String, Object>>> obtenerProductos() {
        List<Map<String, Object>> body = restTemplate.exchange(
                urlInventario + "/api/inventario",
                HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
        ).getBody();

        return ServicioResultado.ok(body != null ? body : List.of());
    }

    /**
     * Fallback invocado automáticamente por Resilience4j cuando:
     *  - MS-Inventario responde con error o timeout, o
     *  - el circuito está OPEN (fallo "rápido", sin llamada de red).
     * La firma debe coincidir con el método original + un Throwable final.
     */
    @SuppressWarnings("unused")
    private ServicioResultado<List<Map<String, Object>>> fallbackObtenerProductos(Throwable ex) {
        return ServicioResultado.fallo(List.of(), "MS-Inventario no disponible: " + ex.getMessage());
    }
}
