package donaton.mspedidos.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

/**
 * Control de seguridad mínimo para las operaciones de escritura de
 * MS-Pedidos. Ver la explicación completa en el equivalente de
 * MS-Inventario ({@code donaton.msinventario.security.ApiKeyFilter}):
 * las lecturas (GET) siguen siendo públicas, pero toda escritura
 * (POST/PUT/DELETE) exige el header {@code X-API-KEY}.
 */
@Component
public class ApiKeyFilter extends HttpFilter {

    private static final Set<String> METODOS_PROTEGIDOS = Set.of("POST", "PUT", "DELETE", "PATCH");
    private static final Set<String> RUTAS_EXCLUIDAS = Set.of(
            "/swagger-ui", "/api-docs", "/actuator");

    @Value("${app.security.api-key:smartlogix-dev-key}")
    private String apiKeyEsperada;

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        boolean esEscritura = METODOS_PROTEGIDOS.contains(request.getMethod());
        boolean esRutaExcluida = RUTAS_EXCLUIDAS.stream().anyMatch(request.getRequestURI()::startsWith);

        if (!esEscritura || esRutaExcluida) {
            chain.doFilter(request, response);
            return;
        }

        String apiKeyRecibida = request.getHeader("X-API-KEY");
        if (apiKeyEsperada.equals(apiKeyRecibida)) {
            chain.doFilter(request, response);
            return;
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"Header X-API-KEY faltante o inválido\"}");
    }
}
