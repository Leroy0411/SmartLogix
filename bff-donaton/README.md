# BFF-Donaton — Backend For Frontend

## Descripción

Capa de composición entre el frontend React y los microservicios internos de la plataforma **SmartLogix**. Agrega y transforma datos de MS-Inventario y MS-Pedidos en responses optimizados para cada vista del cliente.

**Puerto:** `8080`
**Patrones implementados:** Backend For Frontend (BFF) · Circuit Breaker (Resilience4j)

---

## Patrón BFF

El BFF actúa como adaptador inteligente entre el frontend y los microservicios:

```
React App
    │
    │  1 llamada HTTP
    ▼
BFF (puerto 8080)
    ├── InventarioClient → MS-Inventario (8081) → GET /api/inventario
    └── PedidosClient    → MS-Pedidos    (8082) → GET /api/pedidos/centros-distribucion
                                                 → GET /api/pedidos
    │
    │  1 response agregado
    ▼
DashboardResumenDTO
```

**Beneficio:** El frontend realiza **1 llamada** en lugar de 3, recibiendo un payload optimizado. Si los microservicios no están disponibles, el BFF devuelve alertas y los datos disponibles sin fallar.

---

## Patrón Circuit Breaker

`InventarioClient` y `PedidosClient` (paquete `donaton.bff.client`) envuelven cada llamada HTTP con `@CircuitBreaker` de Resilience4j:

- **CLOSED**: las llamadas fluyen normalmente hacia el microservicio.
- **OPEN**: cuando la tasa de fallos/lentitud supera el umbral configurado (`application.properties`), el circuito se abre y las llamadas siguientes se resuelven **instantáneamente por fallback**, sin golpear la red, evitando que una caída aguas abajo cuelgue al BFF con timeouts en cascada.
- **HALF_OPEN**: pasado `wait-duration-in-open-state`, se permiten algunas llamadas de prueba para verificar si el servicio se recuperó antes de volver a CLOSED.

Cada fallback devuelve un `ServicioResultado` marcado como no disponible; `BffService` lo traduce en una alerta legible (`"⚠ MS-Inventario no disponible: ..."`) sin romper el resto del dashboard.

Estado de los circuitos en vivo: `GET http://localhost:8080/actuator/health` y `GET http://localhost:8080/actuator/circuitbreakerevents`.

---

## Requisitos

- Java 21
- Maven 3.9+
- MS-Inventario corriendo en puerto 8081 (opcional para pruebas unitarias)
- MS-Pedidos corriendo en puerto 8082 (opcional para pruebas unitarias)

---

## Instalación y Ejecución

```bash
# 1. Iniciar microservicios primero
cd ms-inventario && mvn spring-boot:run &
cd ms-pedidos    && mvn spring-boot:run &

# 2. Iniciar el BFF
cd bff-donaton
mvn clean compile
mvn test
mvn spring-boot:run
```

BFF disponible en: `http://localhost:8080`
Documentación interactiva (Swagger UI): `http://localhost:8080/swagger-ui.html`
Especificación OpenAPI (JSON): `http://localhost:8080/api-docs`

---

## Endpoints

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/bff/dashboard` | Resumen agregado: inventario + centros + pedidos |
| GET | `/bff/health` | Health check del BFF |

### Respuesta `/bff/dashboard`

```json
{
  "totalProductos": 15,
  "productosDisponibles": 8,
  "productosReservados": 5,
  "productosAgotados": 2,
  "totalCentrosDistribucion": 4,
  "centrosActivos": 3,
  "centrosSaturados": 1,
  "totalPedidos": 10,
  "pedidosPendientes": 3,
  "pedidosEnCamino": 4,
  "pedidosEntregados": 3,
  "alertas": ["🔴 1 centros de distribución saturados"]
}
```

---

## Configuración

En `src/main/resources/application.properties`:

```properties
ms.inventario.url=http://localhost:8081
ms.pedidos.url=http://localhost:8082
```

Para sobreescribir con variables de entorno:

```bash
MS_INVENTARIO_URL=http://ms-inventario:8081 mvn spring-boot:run
```

---

## Pruebas Unitarias

```bash
mvn test
# Reporte JaCoCo: target/site/jacoco/index.html
```

Clases cubiertas:
- `BffServiceTest` — agregación correcta, manejo de fallos parciales de microservicios.
- `BffControllerTest` — contrato HTTP de `/bff/dashboard` y `/bff/health` con `@WebMvcTest`.
- `InventarioClientCircuitBreakerTest` — comportamiento real del Circuit Breaker (transición CLOSED → OPEN, fail-fast).
