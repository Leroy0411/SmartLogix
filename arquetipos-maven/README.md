# Donaton Microservice Archetype

## Descripción

Arquetipo Maven para generar nuevos microservicios de la plataforma **Donaton** con la estructura base predefinida, incluyendo Repository Pattern y pruebas unitarias.

---

## Uso del Arquetipo

### Paso 1 — Instalar el arquetipo en el repositorio local

```bash
cd arquetipos-maven/donaton-microservice-archetype
mvn install
```

### Paso 2 — Generar un nuevo microservicio

```bash
mvn archetype:generate \
  -DarchetypeGroupId=donaton.arquetipos \
  -DarchetypeArtifactId=donaton-microservice-archetype \
  -DarchetypeVersion=1.0.0 \
  -DgroupId=donaton \
  -DartifactId=ms-nuevo \
  -Dversion=1.0.0 \
  -DservicePuerto=8083
```

### Paso 3 — Iniciar el microservicio generado

```bash
cd ms-nuevo
mvn spring-boot:run
```

---

## Qué incluye el arquetipo

| Archivo generado | Descripción |
|-----------------|-------------|
| `pom.xml` | Dependencias Spring Boot 3.x + JaCoCo |
| `Application.java` | Clase principal Spring Boot |
| `model/Entidad.java` | Modelo de dominio base |
| `repository/EntidadRepository.java` | Interfaz Repository Pattern |
| `repository/EntidadRepositoryImpl.java` | Implementación en memoria |
| `service/EntidadService.java` | Servicio de negocio |
| `controller/EntidadController.java` | REST Controller con CRUD completo |
| `service/EntidadServiceTest.java` | Pruebas unitarias con Mockito |
| `application.properties` | Configuración con puerto parametrizado |

---

## Personalización

Después de generar el microservicio:

1. Renombrar `Entidad` por el nombre del dominio real (ej: `Voluntario`)
2. Agregar atributos específicos en el modelo
3. Implementar reglas de negocio en el servicio
4. Si aplica, agregar Factory Method o Observer según la complejidad
