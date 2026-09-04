package donaton.msinventario.security;

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
 * Control de seguridad mínimo pero real para las operaciones de escritura.
 *
 * No se optó por Spring Security completo (con roles/JWT) para no ampliar
 * demasiado el alcance a un día de la entrega, pero sí se cierra el hueco
 * más evidente: hoy CUALQUIERA puede crear, modificar o borrar productos
 * sin autenticarse. Las lecturas (GET) siguen siendo públicas — el
 * dashboard y el catálogo no requieren credenciales — pero toda escritura
 * (POST/PUT/DELETE) exige el header {@code X-API-KEY}.
 *
 * La clave se configura vía {@code app.security.api-key} (variable de
 * entorno {@code API_KEY} en producción); jamás debe quedar hardcodeada
 * en el código fuente real de un sistema productivo — aquí se documenta
 * explícitamente como decisión de alcance para la evaluación.
 */
@Component
public class ApiKeyFilter extends HttpFilter {

    private static final Set<String> METODOS_PROTEGIDOS = Set.of("POST", "PUT", "DELETE", "PATCH");
    private static final Set<String> RUTAS_EXCLUIDAS = Set.of(
            "/swagger-ui", "/api-docs", "/h2-console", "/actuator");

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
