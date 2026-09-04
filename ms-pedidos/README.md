# MS-Pedidos — Microservicio de Procesamiento de Pedidos y Coordinación de Envíos

## Descripción

Microservicio que cubre dos módulos del caso SmartLogix: el **"Procesamiento de Pedidos"** (validación, aprobación y asignación de pedidos, con trazabilidad completa) y la **"Coordinación de Envíos"** (planificación de despacho y comunicación con transportistas). Un mismo `Pedido` recorre ambos: nace `PENDIENTE`, se asigna a un centro de distribución y su ciclo de vida (`EN_CAMINO` → `ENTREGADO`) refleja el estado del despacho.

**Puerto:** `8082`
**Patrones implementados:** Repository Pattern · Observer
**Persistencia:** Spring Data JPA + base de datos H2 (archivo embebido)

---

## Patrones de Diseño

### Repository Pattern
- **Interfaces:** `CentroDistribucionRepository`, `PedidoRepository`, ambas extienden `JpaRepository`
- **Persistencia real:** Hibernate gestiona automáticamente el CRUD sobre la base H2.
- **Beneficio:** Desacopla la lógica de negocio de la persistencia; cambiar de H2 a otro motor relacional en producción solo requiere ajustar `application.properties`.

### Observer (GoF)
- **Interfaz:** `PedidoObserver`
- **Observadores concretos:** `AuditoriaPedidoObserver` (bitácora de trazabilidad), `NotificacionPedidoObserver` (simula alertas al cliente/transportista)
- **Sujeto:** `PedidoService`, que notifica a todos los observadores registrados en cada cambio de estado (creación, despacho, entrega, cancelación)
- **Beneficio:** Agregar una nueva reacción (por ejemplo, métricas o integración con un WMS externo) solo requiere implementar la interfaz, sin tocar `PedidoService` (Open/Closed Principle). Da soporte directo al requisito de trazabilidad del caso.

---

## Persistencia de Datos (JPA)

- **Entidades:** `CentroDistribucion` (tabla `centros_distribucion`), `Pedido` (tabla `pedidos`).
- **Motor:** H2 en modo archivo (`./data/pedidos-db.mv.db`), por lo que los datos **sobreviven a reinicios** del microservicio.
- **DDL automático:** `spring.jpa.hibernate.ddl-auto=update`.
- **Consola de administración:** `http://localhost:8082/h2-console` (usuario `sa`, sin contraseña, URL `jdbc:h2:file:./data/pedidos-db`).
- **Pruebas de integración:** `PedidoRepositoryTest` usa `@DataJpaTest` con H2 en memoria.

---

## Requisitos

- Java 21
- Maven 3.9+

---

## Instalación y Ejecución

```bash
cd ms-pedidos
mvn clean compile
mvn test
mvn spring-boot:run
```

El servicio estará disponible en: `http://localhost:8082`
Documentación interactiva (Swagger UI): `http://localhost:8082/swagger-ui.html`
Especificación OpenAPI (JSON): `http://localhost:8082/api-docs`

---

## Endpoints REST

### Centros de Distribución

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/pedidos/centros-distribucion` | Listar todos los centros |
| GET | `/api/pedidos/centros-distribucion/activos` | Listar centros ACTIVOS |
| GET | `/api/pedidos/centros-distribucion/con-capacidad` | Listar centros activos con capacidad disponible |
| POST | `/api/pedidos/centros-distribucion` | Registrar un nuevo centro |
| PUT | `/api/pedidos/centros-distribucion/{id}/ocupacion` | Actualizar ocupación actual |
| DELETE | `/api/pedidos/centros-distribucion/{id}` | Eliminar un centro |

### Pedidos (con Observer)

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/pedidos` | Listar todos los pedidos |
| GET | `/api/pedidos/estado/{estado}` | Filtrar por estado (`PENDIENTE`, `EN_CAMINO`, `ENTREGADO`, `CANCELADO`) |
| POST | `/api/pedidos` | Crear un pedido (notifica observadores) |
| PUT | `/api/pedidos/{id}/despachar` | Marcar como despachado (`EN_CAMINO`) |
| PUT | `/api/pedidos/{id}/entregar` | Confirmar entrega |
| PUT | `/api/pedidos/{id}/cancelar` | Cancelar pedido |
| DELETE | `/api/pedidos/{id}` | Eliminar un pedido |

### Ejemplo de creación de pedido (POST)

```json
{
  "centroDistribucionId": 1,
  "clienteId": 42,
  "direccionEntrega": "Av. Providencia 1234, Santiago",
  "responsableTransporte": "Juan Pérez",
  "patenteVehiculo": "AB-1234"
}
```

---

## Pruebas Unitarias

```bash
mvn test
# Reporte JaCoCo: target/site/jacoco/index.html
```

Clases cubiertas:
- `PedidoServiceTest` — pruebas del patrón Observer (notificación en cada cambio de estado) y de la gestión de centros de distribución.
- `PedidoControllerTest` — pruebas del contrato HTTP con `@WebMvcTest`.
- `PedidoObserversTest` — pruebas de los observadores concretos (auditoría y notificación).
- `PedidoRepositoryTest` — pruebas de integración JPA con `@DataJpaTest` sobre H2 en memoria.

---

## Estructura del Proyecto

```
ms-pedidos/
├── src/main/java/donaton/mspedidos/
│   ├── model/          → CentroDistribucion.java, Pedido.java (entidades JPA)
│   ├── repository/     → CentroDistribucionRepository, PedidoRepository
│   ├── observer/       → PedidoObserver (interfaz) + AuditoriaPedidoObserver,
│   │                     NotificacionPedidoObserver (concretos)
│   ├── service/        → PedidoService
│   └── controller/     → PedidoController (con anotaciones OpenAPI)
├── src/main/resources/ → application.properties (config H2 + JPA + Swagger)
├── src/test/java/      → PedidoServiceTest, PedidoControllerTest,
│                          PedidoObserversTest, PedidoRepositoryTest
└── src/test/resources/ → application.properties (perfil de test, H2 en memoria)
```
