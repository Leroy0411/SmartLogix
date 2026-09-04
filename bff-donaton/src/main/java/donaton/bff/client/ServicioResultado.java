package donaton.bff.client;

/**
 * Envoltorio del resultado de invocar a un microservicio externo.
 * ─────────────────────────────────────────────────────────────────────────
 * Permite que un método de fallback de Circuit Breaker devuelva "no
 * disponible" sin lanzar una excepción hacia arriba, para que el BFF pueda
 * degradarse de forma controlada (mostrar el resto del dashboard + una
 * alerta) en lugar de romper toda la respuesta.
 */
public final class ServicioResultado<T> {

    private final T datos;
    private final boolean disponible;
    private final String mensajeError;

    private ServicioResultado(T datos, boolean disponible, String mensajeError) {
        this.datos = datos;
        this.disponible = disponible;
        this.mensajeError = mensajeError;
    }

    public static <T> ServicioResultado<T> ok(T datos) {
        return new ServicioResultado<>(datos, true, null);
    }

    public static <T> ServicioResultado<T> fallo(T valorPorDefecto, String mensajeError) {
        return new ServicioResultado<>(valorPorDefecto, false, mensajeError);
    }

    public T getDatos() { return datos; }
    public boolean isDisponible() { return disponible; }
    public String getMensajeError() { return mensajeError; }
}
