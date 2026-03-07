# BillMate – Directrices del Proyecto

## Proyecto

**BillMate** es una aplicación de facturación para pequeños negocios. Monorepo Maven con 4 microservicios Java 21 + Spring Boot 3.3.0 (TFG del ciclo de DAW).

| Servicio | Puerto | Arquitectura |
|---|---|---|
| API Gateway | 8080 | Reactivo (Spring Cloud Gateway + WebFlux) |
| Auth Service | 8081 | Capas (Controller → Service → Repository) |
| Billing Service | 8082 | Hexagonal estricta (Ports & Adapters) |
| Frontend Service | 8083 | SSR (Spring Boot + Thymeleaf + vanilla JS) |

Bases de datos: PostgreSQL 16 — `auth_db` (puerto 5434), `billing_db` (puerto 5433).

## Stack

Java 21, Spring Boot 3.3.0, Spring Cloud 2023.0.3, Spring Security + JWT (jjwt 0.11.5, HS256), Spring Data JPA + PostgreSQL 16, OpenAPI Generator 7.3.0, iTextPDF 5.5.13.3, Thymeleaf, Lombok (excepto dominio billing), Maven multi-módulo, Docker multi-stage (eclipse-temurin:21 Alpine), GitHub Actions, Testcontainers, JUnit 5 + Mockito + MockMvc, logstash-logback-encoder 7.4 (JSON logging).

## Idioma

- Código (clases, métodos, variables): **inglés**
- Mensajes de error al usuario: **español**
- Comentarios y documentación: **español**
- `@DisplayName` en tests: **inglés**

## Convenciones de Código

- **Inyección por constructor** siempre (NUNCA `@Autowired` en campos)
- `@RequiredArgsConstructor` (Lombok) en controllers y filtros
- Constructores explícitos en services de use case (billing)
- Variables en **camelCase**, paquete raíz: `com.billMate`
- Java 21: records para commands e inmutables, text blocks para strings multilínea

## Nomenclatura

| Tipo | Patrón | Ejemplo |
|---|---|---|
| Modelos de dominio | `{Sustantivo}` | `Client`, `Invoice` |
| Use case (interfaz) | `{Verbo}{Sustantivo}UseCase` | `CreateClientUseCase` |
| Use case (impl) | `{Verbo}{Sustantivo}Service` | `CreateClientService` |
| Commands | `{Verbo}{Sustantivo}Command` (record) | `CreateClientCommand` |
| Puertos de salida | `{Sustantivo}RepositoryPort` | `ClientRepositoryPort` |
| Adaptadores JPA | `{Sustantivo}JpaAdapter` | `ClientJpaAdapter` |
| Entidades JPA | `{Sustantivo}Entity` | `ClientEntity` |
| Spring Data repos | `SpringData{Sustantivo}Repository` | `SpringDataClientRepository` |
| Mappers REST | `{Sustantivo}RestMapper` — `toDto()`, `toCreateCommand()` | `ClientRestMapper` |
| Mappers Persistencia | `{Sustantivo}PersistenceMapper` — `toDomain()`, `toEntity()` | `ClientPersistenceMapper` |
| DTOs billing | `{Sustantivo}DTO`, `New{Sustantivo}DTO` | `ClientDTO` |
| DTOs auth | `{Sustantivo}Request/Response` | `LoginRequest` |

## Errores (Billing)

- `GlobalExceptionHandler` con `@RestControllerAdvice`, respuesta `ApiError`
- Mensajes constantes en `ErrorMessages.java`
- Formato: `{ status, code, message, errors[], timestamp }`

## Testing

- JUnit 5 con `@DisplayName` descriptivo en inglés
- Nombres de métodos de test en **camelCase** (no snake_case)
- Sin `public` en clases ni métodos de test (JUnit 5 no lo requiere)
- Perfil: `application-test.yaml` con docker compose deshabilitado

| Tipo de test | Patrón nombre método | Herramientas | Mocks |
|---|---|---|---|
| Dominio (billing) | `shouldDoSomething` | JUnit 5 puro | Ninguno |
| Use case (billing) | `shouldDoSomething` | JUnit 5 + fakes in-memory | Fake (NO Mockito) |
| Controller (billing) | `givenX_whenY_thenZ` | `@WebMvcTest` + MockMvc | `@MockBean` + Mockito |
| Integración (auth) | Descriptivo | Testcontainers (PostgreSQL 16-alpine) | DB real en contenedor |

## Bases de Datos

| Base de datos | Servicio | Puerto | Usuario | Password |
|---|---|---|---|---|
| `auth_db` | Auth Service | 5434 | postgres | postgres |
| `billing_db` | Billing Service | 5433 | postgres | postgres |

### Convenciones SQL

| Concepto | Convención | Ejemplo |
|---|---|---|
| Nombres de tabla | snake_case, plural | `clients`, `invoices` |
| Primary keys | `{tabla_singular}_id` o `id` | `client_id`, `invoice_id` |
| Foreign keys | `{tabla_referenciada_singular}_id` | `client_id` |
| Tipos monetarios | `NUMERIC(10, 2)` | `total`, `unit_price` |
| Porcentajes | `NUMERIC(5, 2)` | `tax_percentage` |
| Timestamps | `TIMESTAMP WITH TIME ZONE` | `created_at` |
| Strings | `VARCHAR(255)` por defecto | — |
| Sequences | `BIGSERIAL` | — |
| FK cascada | `ON DELETE CASCADE` | Siempre |
| Constraint naming | `fk_{tabla}_{referencia}` | `fk_invoices_client` |
| Índices | `idx_{tabla}_{columna}` | `idx_invoices_client_id` |

### Entidades JPA

- Lombok: `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`
- `@Table(name = "nombre_tabla")` explícito
- `@Column(name = "...")` cuando el nombre difiere del campo Java
- `@GeneratedValue(strategy = GenerationType.IDENTITY)` para PKs
- `@Id` siempre de tipo `Long`
- `FetchType.LAZY` por defecto en `@ManyToOne`
- `CascadeType.ALL` + `orphanRemoval = true` en `@OneToMany`
- `open-in-view: false` en configuración

## Docker

### Dockerfiles (Multi-Stage Build)

```dockerfile
# Stage 1: Build
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN ./mvnw dependency:go-offline -B
COPY src src
RUN ./mvnw clean package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE {puerto}
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Reglas:** multi-stage siempre, builder con JDK, runtime con JRE, `dependency:go-offline` para caché, `-DskipTests -B` en build.

### Docker Compose (Desarrollo)

Cada servicio con DB tiene su `docker-compose.yaml` (PostgreSQL 16). Puertos: 5434 (auth), 5433 (billing). Volúmenes nombrados. Spring Boot auto-gestiona el ciclo de vida (`spring-boot-docker-compose`).

## CI/CD

### CI (PRs a `main`)

| Workflow | Trigger |
|---|---|
| `.github/workflows/auth-ci.yaml` | PR con cambios en `auth-service/**` |
| `.github/workflows/billing-ci.yaml` | PR con cambios en `billing-service/**` |
| `.github/workflows/api-gateway-ci.yaml` | PR con cambios en `api-gateway/**` |

Comando: `./mvnw -B -ntp clean verify`. Concurrencia con `cancel-in-progress: true`.

### CD (Tags semánticos)

| Tag pattern | Servicio |
|---|---|
| `auth-v*.*.*` | Auth Service |
| `billing-v*.*.*` | Billing Service |
| `gateway-v*.*.*` | API Gateway |

Flujo: test → build JAR → Docker Buildx → push a **GHCR** con tags `{version}` + `latest`.

```bash
# Crear release
git tag billing-v1.2.0
git push origin billing-v1.2.0
```

## Build y Test

```bash
mvn clean install                                          # Compilar todo
cd billing-service && mvn clean verify                     # Tests de un servicio
cd billing-service && mvn clean compile                    # Regenerar clases OpenAPI
cd auth-service && mvn spring-boot:run                     # Iniciar servicio
docker-compose -f auth-service/docker-compose.yaml up -d   # BD auth
docker-compose -f billing-service/docker-compose.yaml up -d # BD billing
```

## Observabilidad

### Correlation ID

Cada petición entrante recibe un UUID de correlación (`x-Correlation-Id`) que se propaga a través de todos los microservicios:

1. **API Gateway** (`CorrelationIdFilter` — `GlobalFilter + Ordered`): genera UUID si no existe, lo inyecta en header de request (downstream) y response (upstream), y lo coloca en MDC.
2. **Auth / Billing Services** (`CorrelationIdFilter` — `OncePerRequestFilter`): lee el header `x-Correlation-Id` del request y lo coloca en MDC para los logs del servicio.

### Logging Estructurado (JSON)

Todos los servicios usan **logstash-logback-encoder 7.4** para emitir logs en formato JSON con el campo `correlationId` del MDC.

**Configuración:** `logback-spring.xml` con `LogstashEncoder` en cada servicio.

**Patrón de logs con `StructuredArguments.kv()`:**

```java
import static net.logstash.logback.argument.StructuredArguments.kv;

// Mensajes de log en inglés, campos como kv()
log.info("Creating client", kv("nif", command.nif()), kv("name", command.name()));
log.debug("Querying client in DB", kv("clientId", id));
log.warn("Client not found", kv("clientId", clientId));
log.error("Unexpected error", kv("error", ex.getMessage()), ex);
```

**Niveles de log por capa:**

| Capa | Nivel | Ejemplo |
|---|---|---|
| Controllers | `INFO` | Entrada/salida de endpoints (`>> POST /clients`, `<< POST /clients`) |
| Use Cases | `INFO`/`DEBUG` | Lógica de negocio, creación/actualización de entidades |
| Adapters (JPA) | `DEBUG` | Operaciones de persistencia |
| Filters | `DEBUG` | Validación JWT, correlación |
| Exception Handlers | `WARN`/`ERROR` | Errores de validación, recursos no encontrados, errores inesperados |

## Instrucciones Detalladas

Cada servicio tiene su propio `AGENTS.md` con convenciones específicas:

| Archivo | Contenido |
|---|---|
| `api-gateway/AGENTS.md` | Enrutamiento reactivo, filtro JWT, rutas públicas |
| `auth-service/AGENTS.md` | Capas, endpoints, JWT, seguridad, Testcontainers |
| `billing-service/AGENTS.md` | Hexagonal, contract-first, testing con fakes, máquina de estados |
| `frontend-service/AGENTS.md` | Thymeleaf SSR, vanilla JS, comunicación con gateway |
