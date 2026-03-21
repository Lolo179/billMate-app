# BillMate – Directrices del Proyecto

## Proyecto

**BillMate** es una aplicación de facturación para pequeños negocios. Monorepo Maven con 4 microservicios Java 21 + Spring Boot 3.3.0 (TFG del ciclo de DAW).

| Servicio | Puerto | Arquitectura |
|---|---|---|
| API Gateway | 8080 | Reactivo (Spring Cloud Gateway + WebFlux) |
| Auth Service | 8081 | Capas (Controller → Service → Repository) |
| Billing Service | 8082 | Hexagonal estricta (Ports & Adapters) |
| Notification Service | 8084 | Capas (Listener → Log, sin BD) |
| Frontend Service | 8083 | SSR (Spring Boot + Thymeleaf + vanilla JS) |

Bases de datos: PostgreSQL 16 — `auth_db` (puerto 5434), `billing_db` (puerto 5433).

## Stack

Java 21, Spring Boot 3.3.0, Spring Cloud 2023.0.3, Spring Security + JWT (jjwt 0.11.5, HS256), Spring Data JPA + PostgreSQL 16, Apache Kafka 3.8.0 (KRaft) + Spring Kafka, OpenAPI Generator 7.3.0, iTextPDF 5.5.13.3, Thymeleaf, Lombok (excepto dominio billing), Maven multi-módulo, Docker multi-stage (eclipse-temurin:21 Alpine), GitHub Actions, Testcontainers, JUnit 5 + Mockito + MockMvc, logstash-logback-encoder 7.4 (JSON logging).

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
| `.github/workflows/auth-ci.yaml` | PR a `main` |
| `.github/workflows/billing-ci.yaml` | PR a `main` |
| `.github/workflows/api-gateway-ci.yaml` | PR a `main` |
| `.github/workflows/frontend-ci.yaml` | PR a `main` + `workflow_dispatch` |
| `.github/workflows/e2e-ci.yaml` | PR a `main` + `workflow_dispatch` |

Comando servicios Java: `./mvnw -B -ntp clean verify`. Concurrencia con `cancel-in-progress: true`.

### E2E CI — Estrategia híbrida (JVM + Docker)

El workflow `e2e-ci.yaml` levanta el entorno completo para ejecutar las pruebas Playwright:

| Componente | Modo | Justificación |
|---|---|---|
| `auth-db` / `billing-db` / `kafka` | Contenedor Docker | Infraestructura ligera vía `e2e/docker-compose.ci.yaml` |
| `auth-service` | Proceso JVM en el runner | Build rápido, sin imagen Docker |
| `billing-service` | Proceso JVM en el runner | Build rápido, sin imagen Docker |
| `api-gateway` | Proceso JVM en el runner | Build rápido, sin imagen Docker |
| `frontend-service` | Vite dev server (`npm run dev`) | Proxy integrado hacia `localhost:8080` |

**Flujo del workflow:**
1. Build JARs de auth, billing y api-gateway (`mvnw clean package -DskipTests`)
2. Levantar DBs + Kafka con `e2e/docker-compose.ci.yaml`
3. Esperar a que la infraestructura esté lista
4. Arrancar auth-service, billing-service y api-gateway como procesos JVM en background
5. Arrancar frontend con Vite dev server (`VITE_USE_MSW=false`)
6. Esperar health checks de todos los servicios + smoke check de login
7. Ejecutar `npm test` (Playwright) con `E2E_BASE_URL=http://127.0.0.1:5173`

### CD — Pipeline global (push a `main`)

El pipeline de entrega continua está dividido en dos workflows con responsabilidades claras:

```
push a main
    │
  ci.yaml ──┬── build-and-push  (~60 min)  → GHCR :latest
            └── e2e             (~45 min)  → Playwright (needs: build-and-push)
                  │ workflow_run: completed + success
              deploy.yaml
                  └── deploy    (~15 min)  → EC2
```

#### `ci.yaml` — Integración continua (push a `main`)

| Job | Qué hace |
|---|---|
| `build-and-push` | Construye las 4 imágenes Docker y las publica en **GHCR** con tag `:latest` |
| `e2e` | Levanta el entorno completo y ejecuta las pruebas Playwright (`needs: build-and-push`) |

#### `deploy.yaml` — Despliegue continuo (`workflow_run` de CI)

| Job | Qué hace |
|---|---|
| `deploy` | SSH a EC2 → `docker compose pull && up -d --force-recreate` → health check |

EC2 **solo se actualiza si CI completa con éxito** (build + E2E). No existen tags semánticos por servicio; el deploy se activa automáticamente con cada merge a `main`.

## Build y Test

```bash
mvn clean install                                          # Compilar todo
cd billing-service && mvn clean verify                     # Tests de un servicio
cd billing-service && mvn clean compile                    # Regenerar clases OpenAPI
cd auth-service && mvn spring-boot:run                     # Iniciar servicio
docker-compose -f auth-service/docker-compose.yaml up -d   # BD auth
docker-compose -f billing-service/docker-compose.yaml up -d # BD billing
docker-compose -f kafka/docker-compose.yaml up -d           # Kafka broker + Kafka UI
docker-compose -f observability/docker-compose.yaml up -d   # Grafana + Loki + Promtail
```

## Kafka

### Infraestructura

Docker Compose: `kafka/docker-compose.yaml` — Apache Kafka 3.8.0 (KRaft, sin Zookeeper) + Kafka UI (kafbat, puerto `9090`).

**Listeners duales:**
- `PLAINTEXT://kafka:9092` — comunicación entre contenedores Docker
- `PLAINTEXT_HOST://localhost:29092` — acceso desde el host (aplicaciones Spring Boot en desarrollo)

Los servicios Spring Boot configuran `bootstrap-servers: localhost:29092`.

### Eventos

| Topic | Productor | Consumidor | Evento | Patrón |
|---|---|---|---|---|
| `invoice.created` | Billing Service | Notification Service | `InvoiceCreatedEvent` | Fire-and-forget asíncrono (`@Async`) |

### Deserialización entre servicios

El productor (billing-service) serializa `com.billMate.billing.domain.invoice.event.InvoiceCreatedEvent` y el `JsonSerializer` incluye el nombre completo de clase en el header `__TypeId__`. El consumidor (notification-service) tiene su propia réplica en `com.billMate.notification.event.InvoiceCreatedEvent`, por lo que necesita `spring.json.type.mapping` para mapear la clase del productor a la clase local.

### Resiliencia

La publicación de eventos no bloquea el flujo principal. Si Kafka no está disponible, el evento se pierde pero la operación principal (ej: crear factura) se completa con éxito. Doble try-catch: en el use case y en el adaptador Kafka.

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

### Observabilidad Centralizada (Grafana + Loki + Promtail)

Docker Compose: `observability/docker-compose.yaml` — stack de logging centralizado.

| Componente | Imagen | Puerto | Función |
|---|---|---|---|
| Grafana | grafana/grafana:11.0.0 | `3000` | Dashboard y exploración de logs |
| Loki | grafana/loki:3.0.0 | `3100` | Almacenamiento e indexación de logs |
| Promtail | grafana/promtail:3.0.0 | — | Agente que recolecta logs y los envía a Loki |

**Promtail** lee los ficheros `../logs/*.log` (JSON emitidos por logstash-logback-encoder) y extrae los labels `service` y `level` mediante pipeline stages. Grafana tiene Loki configurado como datasource por defecto vía provisioning (`grafana-datasource.yaml`).

**Credenciales Grafana:** `admin` / `admin` (acceso anónimo como Viewer habilitado).

**Query LogQL ejemplo:** `{service="notification"} |= "invoice.created"`

```bash
docker-compose -f observability/docker-compose.yaml up -d   # Grafana + Loki + Promtail
```

## Instrucciones Detalladas

Cada servicio tiene su propio `AGENTS.md` con convenciones específicas:

| Archivo | Contenido |
|---|---|
| `api-gateway/AGENTS.md` | Enrutamiento reactivo, filtro JWT, rutas públicas |
| `auth-service/AGENTS.md` | Capas, endpoints, JWT, seguridad, Testcontainers |
| `billing-service/AGENTS.md` | Hexagonal, contract-first, testing con fakes, máquina de estados |
| `notification-service/AGENTS.md` | Consumidor Kafka, simulación de email, sin BD |
| `frontend-service/AGENTS.md` | Thymeleaf SSR, vanilla JS, comunicación con gateway |
