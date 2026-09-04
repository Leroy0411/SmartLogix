#  SmartLogix — Plataforma de Gestión de Inventario, Pedidos y Envíos

> Evaluación Parcial 3 | DSY1106 Desarrollo Fullstack III | DuocUC 2026

**Autor:** Leroy Rodriguez
**Profesor:** Cristián Garcia Gutiérrez

---

## Descripción

SmartLogix es una plataforma de gestión logística para PYMEs de eCommerce, basada en **arquitectura de microservicios**. Este repositorio contiene todos los componentes de la Evaluación Parcial 3: integración completa de frontend, BFF, microservicios con persistencia real (JPA + H2), documentación de API (Swagger/OpenAPI) y pruebas unitarias con cobertura medible.

Cubre los tres módulos del caso: **Gestión de Inventario** (`ms-inventario`), **Procesamiento de Pedidos** y **Coordinación de Envíos** (ambos unificados en `ms-pedidos`, ya que un mismo pedido recorre su validación, asignación a un centro de distribución y despacho).

---

## Estructura del Repositorio

```
smartlogix/
├── ms-inventario/          → Microservicio de gestión de inventario (puerto 8081)
├── ms-pedidos/              → Microservicio de pedidos y coordinación de envíos (puerto 8082)
├── bff-donaton/            → Backend For Frontend (puerto 8080)
├── frontend-donaton/       → Frontend React con Vite (puerto 5173)
├── arquetipos-maven/       → Arquetipo Maven para nuevos microservicios
├── documentacion/          → PDFs de análisis de patrones, branching y repositorios
├── docker-compose.yml      → Levanta toda la plataforma con un solo comando
└── setup-git.sh            → Referencia/reproducción de la estrategia de branching
```

---

## Arquitectura de Microservicios

```
                         ┌──────────────────────┐
                         │   Frontend React      │
                         │   (Vite, puerto 5173) │
                         └──────────┬─────────────┘
                                    │ HTTP (axios)
                                    ▼
                         ┌──────────────────────┐
                         │   BFF-Donaton          │
                         │   (puerto 8080)        │
                         └────────┬─────────┬────┘
                   GET /api/inventario   GET /api/pedidos/*
                                  │             │
                       ┌──────────▼───┐   ┌─────▼─────────┐
                       │ MS-Inventario │   │ MS-Pedidos     │
                       │ (puerto 8081) │   │ (puerto 8082)  │
                       └──────┬────────┘   └──────┬─────────┘
                              │ JPA/Hibernate      │ JPA/Hibernate
                       ┌──────▼────────┐   ┌──────▼─────────┐
                       │  H2 (archivo)  │   │  H2 (archivo)  │
                       │ inventario-db  │   │  pedidos-db    │
                       └────────────────┘   └────────────────┘
```

- El **frontend** consume el **BFF** para la vista de dashboard (1 sola llamada agregada) y los **microservicios directamente** para operaciones de escritura (registrar producto, despachar pedido, etc.).
- El **BFF** agrega datos de ambos microservicios mediante `RestTemplate` protegido con **Circuit Breaker**, maneja fallos parciales (si un microservicio cae, el BFF sigue respondiendo con los datos disponibles y una alerta).
- **MS-Pedidos también llama a MS-Inventario** (mismo patrón Circuit Breaker) al despachar un pedido, para descontar el stock del producto despachado — es la integración real entre "Coordinación de Envíos" y "Gestión de Inventario" que exige el caso (antes el endpoint de descuento existía pero nadie lo invocaba).
- Cada **microservicio** persiste sus datos en su propia base **H2** embebida (archivo local), de forma completamente independiente — no comparten base de datos (principio de microservicios: *Database per Service*).

---

## Patrones de Diseño Implementados

| Patrón | Componente | Beneficio |
|--------|-----------|-----------|
| **Repository Pattern** | MS-Inventario + MS-Pedidos | Desacopla la lógica de negocio del motor de persistencia (Spring Data JPA) |
| **Factory Method** | MS-Inventario | Crea productos por categoría (Electrónica, Alimentos, Hogar, Ropa) sin modificar código existente |
| **Observer** | MS-Pedidos | Notifica cambios de estado de pedidos a múltiples receptores (auditoría, notificación a transportista/cliente), dando trazabilidad al procesamiento y envío |
| **Backend For Frontend** | BFF-Donaton | Agrega datos de ambos microservicios en 1 llamada HTTP |
| **Circuit Breaker** (Resilience4j) | BFF-Donaton → MS-Inventario / MS-Pedidos, **y** MS-Pedidos → MS-Inventario | Evita fallos en cascada: si un microservicio cae o se cuelga, el circuito abre y quien llama responde con fallback (+ alerta, en el caso del BFF) en vez de esperar timeouts indefinidos |
| **Facade** | Frontend React | Encapsula todas las llamadas HTTP en una interfaz simple |
| **Custom Hook** | Frontend React | Encapsula estado asíncrono y lógica de fetching |

---

## Seguridad

Los 3 servicios backend aplican el mismo esquema, deliberadamente simple para el
alcance de esta entrega (no se usó Spring Security completo con roles/JWT):

- **CORS restringido**: cada servicio define `app.cors.allowed-origin` (por defecto
  `http://localhost:5173`, el frontend) en vez del `@CrossOrigin(origins = "*")`
  original — ver `CorsConfig` en cada módulo.
- **API key en escrituras**: todo `POST` / `PUT` / `DELETE` en MS-Inventario y
  MS-Pedidos exige el header `X-API-KEY` (`ApiKeyFilter`). Las lecturas (`GET`)
  siguen siendo públicas. El valor por defecto para desarrollo es
  `smartlogix-dev-key` (`app.security.api-key`, sobrescribible con la variable de
  entorno `API_KEY`); el frontend y la llamada interna de MS-Pedidos a
  MS-Inventario ya la incluyen automáticamente.
- **Manejo global de excepciones** (`GlobalExceptionHandler`): ningún error no
  controlado expone stack traces; los errores de validación devuelven 400 con el
  detalle por campo, los conflictos de estado devuelven 409.
- **Consola H2** (`/h2-console`): sigue habilitada para la demo (permite mostrar
  la persistencia real en vivo), pero solo es accesible localmente
  (`spring.h2.console.settings.web-allow-others=false`); se recomienda
  deshabilitarla por completo fuera de un entorno de desarrollo/demo.

**Para probar un endpoint de escritura desde Swagger UI**, hay que hacer clic en
"Authorize" (o agregar manualmente el header `X-API-KEY: smartlogix-dev-key`) antes
de ejecutar un `POST`/`PUT`/`DELETE`.

---

## Persistencia de Datos

Ambos microservicios usan **Spring Data JPA** sobre una base de datos **H2** embebida en modo archivo (no en memoria), por lo que los datos **persisten entre reinicios** del servicio:

- `ms-inventario` → `./data/inventario-db.mv.db`
- `ms-pedidos` → `./data/pedidos-db.mv.db`

Cada microservicio expone una consola web de administración H2 (`/h2-console`) para inspeccionar las tablas directamente. El esquema se genera automáticamente desde las entidades anotadas con `@Entity` (`spring.jpa.hibernate.ddl-auto=update`). Ver el detalle en el README de cada microservicio.

---

## Documentación de API (Swagger / OpenAPI)

Los 3 servicios backend (`ms-inventario`, `ms-pedidos`, `bff-donaton`) incluyen `springdoc-openapi`, que genera documentación interactiva automáticamente a partir del código:

| Servicio | Swagger UI | OpenAPI JSON |
|----------|-----------|--------------|
| MS-Inventario | http://localhost:8081/swagger-ui.html | http://localhost:8081/api-docs |
| MS-Pedidos | http://localhost:8082/swagger-ui.html | http://localhost:8082/api-docs |
| BFF-Donaton | http://localhost:8080/swagger-ui.html | http://localhost:8080/api-docs |

Desde Swagger UI se pueden probar directamente los endpoints (incluye ejemplos de petición/respuesta) sin necesidad de Postman.

---

## Inicio Rápido

### Opción A: docker-compose (recomendado para la demo en vivo)

```bash
docker compose up --build
# ms-inventario → http://localhost:8081 | ms-pedidos → http://localhost:8082
# bff-donaton   → http://localhost:8080 | frontend    → http://localhost:5173
```

Levanta los 4 componentes con un solo comando — evita depender de 4 terminales
abiertas durante los 15 minutos de la defensa oral. La API key compartida y el
origen CORS se pueden sobrescribir con las variables de entorno `API_KEY` y
`CORS_ALLOWED_ORIGIN` antes de levantar (ver `docker-compose.yml`).

### Opción B: cada componente por separado

### Requisitos
- Java 21+
- Maven 3.9+
- Node.js 18+

### 1. MS-Inventario (Terminal 1)
```bash
cd ms-inventario
mvn spring-boot:run
# → http://localhost:8081/api/inventario
# → http://localhost:8081/swagger-ui.html
```

### 2. MS-Pedidos (Terminal 2)
```bash
cd ms-pedidos
mvn spring-boot:run
# → http://localhost:8082/api/pedidos/centros-distribucion
# → http://localhost:8082/swagger-ui.html
```

### 3. BFF (Terminal 3)
```bash
cd bff-donaton
mvn spring-boot:run
# → http://localhost:8080/bff/dashboard
# → http://localhost:8080/swagger-ui.html
```

### 4. Frontend (Terminal 4)
```bash
cd frontend-donaton
npm install
npm run dev
# → http://localhost:5173
```

---

## Pruebas Unitarias y Cobertura

```bash
# MS-Inventario (Service, Controller, Factories, Repository JPA, ApiKeyFilter)
cd ms-inventario && mvn verify
# Reporte JaCoCo: target/site/jacoco/index.html

# MS-Pedidos (Service, Controller, Observers, Repository JPA, InventarioClient, ApiKeyFilter)
cd ms-pedidos && mvn verify
# Reporte JaCoCo: target/site/jacoco/index.html

# BFF (Service, Controller, Circuit Breaker)
cd bff-donaton && mvn verify
# Reporte JaCoCo: target/site/jacoco/index.html

# Frontend (componentes, hooks, servicios)
cd frontend-donaton && npm run test:coverage
# Reporte HTML: coverage/index.html
```

Usar `mvn verify` (no solo `mvn test`): el mínimo de **60% de cobertura de línea**
ahora está codificado como una regla de JaCoCo (`jacoco:check`) en los 3 `pom.xml`
de los backends — si un módulo cae bajo el 60%, `mvn verify` **falla el build**
en vez de solo mostrarlo en un reporte que nadie revisa.

Resumen de clases de prueba por componente:

| Componente | Clases de test | Qué cubren |
|---|---|---|
| MS-Inventario | `InventarioServiceTest`, `InventarioControllerTest`, `ProductoFactoriesImplTest`, `ProductoFactoryProviderTest`, `ProductoRepositoryTest`, `ApiKeyFilterTest` | Reglas de negocio por categoría, contrato HTTP, validación (`@Valid`), Factory Method, persistencia JPA, filtro de seguridad |
| MS-Pedidos | `PedidoServiceTest`, `PedidoControllerTest`, `PedidoObserversTest`, `PedidoRepositoryTest`, `InventarioClientCircuitBreakerTest`, `ApiKeyFilterTest` | Patrón Observer, contrato HTTP, transiciones de estado válidas, reserva/liberación de capacidad del centro, integración con MS-Inventario, persistencia JPA, filtro de seguridad |
| BFF-Donaton | `BffServiceTest`, `BffControllerTest`, `InventarioClientCircuitBreakerTest` | Agregación de datos, manejo de fallos parciales, contrato HTTP, comportamiento real del Circuit Breaker |
| Frontend | `donatonApi.test.js`, `useProductos.test.js`, `Dashboard.test.jsx`, `ProductoForm.test.jsx`, `App.test.jsx` | Facade HTTP (incluye el header `X-API-KEY`), Custom Hook, componentes UI, navegación |


---

## API Reference (resumen)

`🔒` = requiere header `X-API-KEY`.

### MS-Inventario (8081)
| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/inventario` | Listar todos los productos |
| POST 🔒 | `/api/inventario` | Crear producto (activa Factory Method; valida con `@Valid`) |
| PUT 🔒 | `/api/inventario/{id}/estado` | Actualizar estado (solo DISPONIBLE/RESERVADO/AGOTADO) |
| PUT 🔒 | `/api/inventario/{id}/descontar-stock` | Descontar stock (cantidad debe ser > 0) |
| DELETE 🔒 | `/api/inventario/{id}` | Eliminar |

### MS-Pedidos (8082)
| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/pedidos/centros-distribucion` | Listar centros de distribución |
| POST 🔒 | `/api/pedidos/centros-distribucion` | Crear centro |
| POST 🔒 | `/api/pedidos` | Crear pedido (valida que el centro exista, esté ACTIVO y tenga capacidad; reserva un espacio) |
| PUT 🔒 | `/api/pedidos/{id}/despachar` | Despachar — solo si está PENDIENTE; libera el espacio del centro y descuenta stock en MS-Inventario si el pedido tiene `productoId`/`cantidadProducto` |
| PUT 🔒 | `/api/pedidos/{id}/entregar` | Confirmar entrega — solo si está EN_CAMINO |
| PUT 🔒 | `/api/pedidos/{id}/cancelar` | Cancelar — solo si está PENDIENTE |

Ciclo de vida del pedido (aplicado con guard clauses en `PedidoService`, no solo documentado):

```
PENDIENTE ──despachar──▶ EN_CAMINO ──entregar──▶ ENTREGADO
    └──────────cancelar──────────▶ CANCELADO
```

### BFF (8080)
| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/bff/dashboard` | Resumen agregado de ambos microservicios |
| GET | `/bff/health` | Health check |

Ver Swagger UI de cada servicio para el detalle completo de endpoints, parámetros y ejemplos.

---

## Estrategia de Branching

Se utilizó **Git Flow adaptado**, con un historial de commits y merges real
(no solo documentado): `git log --oneline --graph --all` desde la raíz del
repositorio muestra la secuencia completa. `setup-git.sh` documenta y
reproduce esa misma secuencia contra un repositorio remoto nuevo si hace falta.

```
main
 └── develop
      ├── feature/arquetipos-maven
      ├── feature/ms-inventario-repository
      ├── feature/ms-inventario-factory
      ├── feature/ms-inventario-jpa-persistence
      ├── feature/ms-inventario-tests
      ├── feature/ms-inventario-security
      ├── feature/ms-pedidos-repository
      ├── feature/ms-pedidos-observer
      ├── feature/ms-pedidos-jpa-persistence
      ├── feature/ms-pedidos-tests
      ├── feature/ms-pedidos-inventario-integration
      ├── feature/ms-pedidos-security
      ├── feature/bff-donaton
      ├── feature/bff-tests
      ├── feature/bff-security
      ├── feature/frontend-smartlogix
      ├── feature/frontend-tests
      └── feature/docker-compose
```

---

## Notas de esta iteración (retrospectiva)

Tras una revisión contra la pauta de evaluación, se cerraron los siguientes
puntos respecto de la versión anterior — útil como insumo directo para el
apartado B/C del informe (decisiones de desarrollo y mejoras identificadas):

- **Integración real pedidos↔inventario**: el descuento de stock al despachar
  un pedido no estaba conectado a nada; ahora `MS-Pedidos` invoca a
  `MS-Inventario` (con Circuit Breaker) en el momento del despacho.
- **Reglas de negocio antes ausentes**: un pedido podía "despacharse" dos
  veces o cancelarse ya entregado; un centro de distribución podía quedar
  sobre-asignado sin límite. Ambas cosas ahora se validan explícitamente en
  `PedidoService`.
- **Seguridad**: no existía ningún control sobre quién podía escribir en los
  microservicios (`CORS` abierto a cualquier origen, sin autenticación). Se
  agregó CORS restringido, un filtro de API key en las escrituras y manejo
  global de excepciones (ver sección Seguridad).
- **Cobertura verificada, no solo declarada**: se agregó `jacoco:check` con
  60% mínimo para que el build efectivamente falle si no se cumple.
- **Pendiente / fuera de alcance deliberado**: no se implementó Spring
  Security completo (JWT/roles) ni mensajería asíncrona real (Kafka/RabbitMQ)
  para la integración entre microservicios — se optó por una llamada síncrona
  con Circuit Breaker, documentado como decisión de alcance. También sigue
  pendiente exponer en el frontend un formulario para crear/despachar pedidos
  (hoy esa parte del flujo se demuestra vía Swagger UI).

---

## Documentación

- 📄 `documentacion/analisis-patrones-y-arquetipos.pdf`
- 📄 `documentacion/plan-branching.pdf`
- 📄 `documentacion/repositorios.txt` — actualizar `TU_USUARIO_GITHUB` con tu usuario real antes de entregar
- 📊 `documentacion/cobertura/frontend-coverage/index.html` — reporte de cobertura del frontend (Vitest), generado sobre la versión previa del dominio; se recomienda regenerarlo con `npm run test:coverage` tras este refactor

