# 🚚 SmartLogix — Plataforma de Gestión de Inventario, Pedidos y Envíos

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
└── documentacion/          → PDFs de análisis de patrones, branching y repositorios
```

> **Nota:** los directorios `bff-donaton` y `frontend-donaton` conservan su nombre de carpeta original de esta entrega; su contenido interno (endpoints, DTOs, componentes, textos) ya fue actualizado íntegramente al dominio SmartLogix.

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
- Cada **microservicio** persiste sus datos en su propia base **H2** embebida (archivo local), de forma completamente independiente — no comparten base de datos (principio de microservicios: *Database per Service*).

---

## Patrones de Diseño Implementados

| Patrón | Componente | Beneficio |
|--------|-----------|-----------|
| **Repository Pattern** | MS-Inventario + MS-Pedidos | Desacopla la lógica de negocio del motor de persistencia (Spring Data JPA) |
| **Factory Method** | MS-Inventario | Crea productos por categoría (Electrónica, Alimentos, Hogar, Ropa) sin modificar código existente |
| **Observer** | MS-Pedidos | Notifica cambios de estado de pedidos a múltiples receptores (auditoría, notificación a transportista/cliente), dando trazabilidad al procesamiento y envío |
| **Backend For Frontend** | BFF-Donaton | Agrega datos de ambos microservicios en 1 llamada HTTP |
| **Circuit Breaker** (Resilience4j) | BFF-Donaton → MS-Inventario / MS-Pedidos | Evita fallos en cascada: si un microservicio cae o se cuelga, el circuito abre y el BFF responde con fallback + alerta en vez de esperar timeouts indefinidos |
| **Facade** | Frontend React | Encapsula todas las llamadas HTTP en una interfaz simple |
| **Custom Hook** | Frontend React | Encapsula estado asíncrono y lógica de fetching |

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
# MS-Inventario (Service, Controller, Factories, Repository JPA)
cd ms-inventario && mvn test
# Reporte JaCoCo: target/site/jacoco/index.html

# MS-Pedidos (Service, Controller, Observers, Repository JPA)
cd ms-pedidos && mvn test
# Reporte JaCoCo: target/site/jacoco/index.html

# BFF (Service, Controller, Circuit Breaker)
cd bff-donaton && mvn test
# Reporte JaCoCo: target/site/jacoco/index.html

# Frontend (componentes, hooks, servicios)
cd frontend-donaton && npm run test:coverage
# Reporte HTML: coverage/index.html
```

Todos los componentes backend apuntan al **60% de cobertura mínimo** exigido.

Resumen de clases de prueba por componente:

| Componente | Clases de test | Qué cubren |
|---|---|---|
| MS-Inventario | `InventarioServiceTest`, `InventarioControllerTest`, `ProductoFactoriesImplTest`, `ProductoFactoryProviderTest`, `ProductoRepositoryTest` | Reglas de negocio por categoría, contrato HTTP, Factory Method, persistencia JPA |
| MS-Pedidos | `PedidoServiceTest`, `PedidoControllerTest`, `PedidoObserversTest`, `PedidoRepositoryTest` | Patrón Observer, contrato HTTP, persistencia JPA |
| BFF-Donaton | `BffServiceTest`, `BffControllerTest`, `InventarioClientCircuitBreakerTest` | Agregación de datos, manejo de fallos parciales, contrato HTTP, comportamiento real del Circuit Breaker |
| Frontend | `donatonApi.test.js`, `useProductos.test.js`, `Dashboard.test.jsx`, `ProductoForm.test.jsx`, `App.test.jsx` | Facade HTTP, Custom Hook, componentes UI, navegación |

> **Nota:** este entorno de generación no tiene acceso a Maven Central, por lo que las pruebas de los módulos Java fueron escritas y revisadas manualmente pero **deben ejecutarse con `mvn test`** en un entorno con acceso normal a internet para confirmar que compilan y generar los reportes JaCoCo definitivos.

---

## API Reference (resumen)

### MS-Inventario (8081)
| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/inventario` | Listar todos los productos |
| POST | `/api/inventario` | Crear producto (activa Factory Method) |
| PUT | `/api/inventario/{id}/estado` | Actualizar estado |
| PUT | `/api/inventario/{id}/descontar-stock` | Descontar stock |
| DELETE | `/api/inventario/{id}` | Eliminar |

### MS-Pedidos (8082)
| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/pedidos/centros-distribucion` | Listar centros de distribución |
| POST | `/api/pedidos/centros-distribucion` | Crear centro |
| POST | `/api/pedidos` | Crear pedido |
| PUT | `/api/pedidos/{id}/despachar` | Despachar (notifica Observer) |
| PUT | `/api/pedidos/{id}/entregar` | Confirmar entrega |

### BFF (8080)
| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/bff/dashboard` | Resumen agregado de ambos microservicios |
| GET | `/bff/health` | Health check |

Ver Swagger UI de cada servicio para el detalle completo de endpoints, parámetros y ejemplos.

---

## Estrategia de Branching

Se utilizó **Git Flow adaptado**:

```
main
 └── develop
      ├── feature/ms-inventario-repository
      ├── feature/ms-inventario-factory
      ├── feature/ms-inventario-jpa-persistence
      ├── feature/ms-inventario-tests
      ├── feature/ms-pedidos-repository
      ├── feature/ms-pedidos-observer
      ├── feature/ms-pedidos-jpa-persistence
      ├── feature/ms-pedidos-tests
      ├── feature/bff-circuit-breaker
      ├── feature/bff-swagger
      ├── feature/frontend-smartlogix
      ├── feature/frontend-tests
      └── feature/arquetipos-maven
```

---

## Documentación

- 📄 `documentacion/analisis-patrones-y-arquetipos.pdf`
- 📄 `documentacion/plan-branching.pdf`
- 📄 `documentacion/repositorios.txt`
- 📊 `documentacion/cobertura/frontend-coverage/index.html` — reporte de cobertura del frontend (Vitest), generado sobre la versión previa del dominio; se recomienda regenerarlo con `npm run test:coverage` tras este refactor

