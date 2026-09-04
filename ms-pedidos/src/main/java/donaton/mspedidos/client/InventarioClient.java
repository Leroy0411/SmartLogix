package donaton.mspedidos.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * PATRÓN: Circuit Breaker (Resilience4j)
 * ─────────────────────────────────────────────────────────────────────────
 * Cierra la integración real entre "Coordinación de Envíos" (MS-Pedidos) y
 * "Gestión de Inventario" (MS-Inventario): al despachar un pedido, MS-Pedidos
 * le pide a MS-Inventario que descuente el stock del producto despachado.
 *
 * Es una llamada "best effort" protegida con el mismo patrón que usa el BFF:
 * si MS-Inventario está caído o lento, el circuito abre y el despacho del
 * pedido en MS-Pedidos NO se revierte (evitar un fallo en cascada es más
 * importante que una consistencia estrictamente transaccional entre dos
 * bases de datos independientes — principio "Database per Service"). La
 * discrepancia queda registrada en el log para reconciliación posterior;
 * en un sistema productivo esto se resolvería con mensajería asíncrona
 * (p. ej. un evento en un tópico que MS-Inventario consume) en vez de una
 * llamada síncrona, pero eso excede el alcance de esta entrega.
 */
@Component
public class InventarioClient {

    private static final Logger log = LoggerFactory.getLogger(InventarioClient.class);

    private final RestTemplate restTemplate;

    @Value("${ms.inventario.url:http://localhost:8081}")
    private String urlInventario;

    /**
     * Clave de servicio-a-servicio: MS-Inventario exige X-API-KEY en sus
     * endpoints de escritura (ver ApiKeyFilter), así que esta llamada
     * interna debe presentarla igual que lo haría el frontend.
     */
    @Value("${app.security.api-key:smartlogix-dev-key}")
    private String apiKey;

    public InventarioClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @CircuitBreaker(name = "msInventario", fallbackMethod = "fallbackDescontarStock")
    public boolean descontarStock(Long productoId, Integer cantidad) {
        String url = urlInventario + "/api/inventario/" + productoId + "/descontar-stock?cantidad=" + cantidad;
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-KEY", apiKey);
        restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(headers), Void.class);
        return true;
    }

    @SuppressWarnings("unused")
    private boolean fallbackDescontarStock(Long productoId, Integer cantidad, Throwable ex) {
        log.warn("No se pudo descontar stock en MS-Inventario (producto {}, cantidad {}): {}. " +
                        "El pedido igual se despachó; se requiere reconciliación manual del stock.",
                productoId, cantidad, ex.getMessage());
        return false;
    }
}
