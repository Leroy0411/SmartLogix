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
 * Encapsula la comunicación HTTP del BFF con MS-Pedidos (puerto 8082).
 * Comparte un único circuito ("msPedidos") entre ambos endpoints
 * consumidos (centros de distribución y pedidos) porque ambos dependen de
 * la disponibilidad del mismo microservicio: si uno falla por caída del
 * servicio, el otro fallará por la misma causa, así que deben contabilizarse
 * juntos.
 *
 * Ver InventarioClient para el detalle de funcionamiento del patrón.
 */
@Component
public class PedidosClient {

    private final RestTemplate restTemplate;

    @Value("${ms.pedidos.url:http://localhost:8082}")
    private String urlPedidos;

    public PedidosClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @CircuitBreaker(name = "msPedidos", fallbackMethod = "fallbackObtenerCentros")
    public ServicioResultado<List<Map<String, Object>>> obtenerCentros() {
        List<Map<String, Object>> body = restTemplate.exchange(
                urlPedidos + "/api/pedidos/centros-distribucion",
                HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
        ).getBody();

        return ServicioResultado.ok(body != null ? body : List.of());
    }

    @CircuitBreaker(name = "msPedidos", fallbackMethod = "fallbackObtenerPedidos")
    public ServicioResultado<List<Map<String, Object>>> obtenerPedidos() {
        List<Map<String, Object>> body = restTemplate.exchange(
                urlPedidos + "/api/pedidos",
                HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
        ).getBody();

        return ServicioResultado.ok(body != null ? body : List.of());
    }

    @SuppressWarnings("unused")
    private ServicioResultado<List<Map<String, Object>>> fallbackObtenerCentros(Throwable ex) {
        return ServicioResultado.fallo(List.of(), "MS-Pedidos (centros de distribución) no disponible: " + ex.getMessage());
    }

    @SuppressWarnings("unused")
    private ServicioResultado<List<Map<String, Object>>> fallbackObtenerPedidos(Throwable ex) {
        return ServicioResultado.fallo(List.of(), "MS-Pedidos (pedidos) no disponible: " + ex.getMessage());
    }
}
