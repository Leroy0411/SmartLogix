# MS-Inventario — Microservicio de Gestión de Inventario

## Descripción

Microservicio del módulo **"Gestión de Inventario"** del caso SmartLogix. Gestiona el ciclo de vida del stock de productos por bodega: ingreso, categorización, reserva y agotamiento, dando soporte a la sincronización de niveles de stock en tiempo real que requieren las PYMEs de eCommerce.

**Puerto:** `8081`
**Patrones implementados:** Repository Pattern · Factory Method
**Persistencia:** Spring Data JPA + base de datos H2 (archivo embebido)

---

## Patrones de Diseño

### Repository Pattern
- **Interfaz:** `ProductoRepository`, extiende `JpaRepository<Producto, Long>`
- **Persistencia real:** Hibernate gestiona automáticamente el CRUD sobre la base H2; no existe código SQL manual.
- **Beneficio:** La lógica de negocio (`InventarioService`) nunca accede directamente al motor de base de datos. Cambiar de H2 a PostgreSQL/MySQL en producción solo requiere ajustar `application.properties`.

### Factory Method (GoF)
- **Clase base abstracta:** `ProductoFactory`
- **Registro de fábricas:** `ProductoFactoryProvider`
- **Fábricas concretas:** `ProductoElectronicaFactory`, `ProductoAlimentosFactory`, `ProductoHogarFactory`, `ProductoRopaFactory`
- **Beneficio:** Agregar una nueva categoría de producto solo requiere una nueva clase sin modificar el código existente (Open/Closed Principle).

---

## Persistencia de Datos (JPA)

- **Entidad:** `Producto` (anotada con `@Entity`, tabla `productos`).
- **Motor:** H2 en modo archivo (`./data/inventario-db.mv.db`), por lo que los datos **sobreviven a reinicios** del microservicio.
- **DDL automático:** `spring.jpa.hibernate.ddl-auto=update` — Hibernate crea/actualiza el esquema según la entidad.
- **Consola de administración:** con el servicio corriendo, abrir `http://localhost:8081/h2-console` y usar la URL `jdbc:h2:file:./data/inventario-db` (usuario `sa`, sin contraseña) para inspeccionar las tablas directamente.
- **Pruebas de integración:** `ProductoRepositoryTest` usa `@DataJpaTest` con una base H2 en memoria (perfil de test) para validar el correcto funcionamiento de la capa de persistencia sin afectar los datos reales.

---

## Requisitos

- Java 21
- Maven 3.9+

---

## Instalación y Ejecución

```bash
# Clonar repositorio
git clone <URL_REPOSITORIO>
cd ms-inventario

# Compilar
mvn clean compile

# Ejecutar pruebas unitarias (con reporte JaCoCo)
mvn test

# Iniciar el servidor
mvn spring-boot:run
```

El servicio estará disponible en: `http://localhost:8081`
Documentación interactiva (Swagger UI): `http://localhost:8081/swagger-ui.html`
Especificación OpenAPI (JSON): `http://localhost:8081/api-docs`

---

## Endpoints REST

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/inventario` | Listar todos los productos |
| GET | `/api/inventario/{id}` | Obtener producto por ID |
| GET | `/api/inventario/estado/{estado}` | Filtrar por estado (`DISPONIBLE`, `RESERVADO`, `AGOTADO`) |
| GET | `/api/inventario/bodega/{bodegaId}` | Filtrar por bodega |
| POST | `/api/inventario` | Registrar producto (usa Factory Method) |
| PUT | `/api/inventario/{id}/estado` | Actualizar estado |
| PUT | `/api/inventario/{id}/descontar-stock` | Descontar stock (p. ej. al confirmarse un pedido) |
| DELETE | `/api/inventario/{id}` | Eliminar producto |

### Ejemplo de creación (POST)

```json
{
  "categoria": "ELECTRONICA",
  "proveedor": "Distribuidora Central",
  "stock": 150,
  "bodegaId": 1,
  "descripcion": "Notebooks 14\" - lote de reposición"
}
```

Categorías válidas: `ELECTRONICA` | `ALIMENTOS` | `HOGAR` | `ROPA`

---

## Pruebas Unitarias

```bash
# Ejecutar pruebas
mvn test

# Ver reporte de cobertura (JaCoCo)
# Abrir: target/site/jacoco/index.html
```

Clases cubiertas:
- `InventarioServiceTest` — pruebas de creación, consulta, actualización, descuento de stock y eliminación (con mocks de Mockito).
- `InventarioControllerTest` — pruebas del contrato HTTP con `@WebMvcTest` (status codes, payloads, errores).
- `ProductoFactoriesImplTest` — pruebas de las reglas de negocio de cada fábrica concreta.
- `ProductoFactoryProviderTest` — pruebas de resolución de fábricas por categoría.
- `ProductoRepositoryTest` — pruebas de integración JPA con `@DataJpaTest` sobre H2 en memoria.

---

## Estructura del Proyecto

```
ms-inventario/
├── src/main/java/donaton/msinventario/
│   ├── model/          → Producto.java (entidad JPA)
│   ├── repository/     → ProductoRepository (Spring Data JPA)
│   ├── factory/        → ProductoFactory (abstracta) + fábricas concretas
│   │                     ProductoFactoryProvider (registro)
│   ├── service/        → InventarioService
│   └── controller/     → InventarioController (con anotaciones OpenAPI)
├── src/main/resources/ → application.properties (config H2 + JPA + Swagger)
├── src/test/java/      → InventarioServiceTest, InventarioControllerTest,
│                          ProductoFactoriesImplTest, ProductoFactoryProviderTest,
│                          ProductoRepositoryTest
└── src/test/resources/ → application.properties (perfil de test, H2 en memoria)
```
