# BillMate – Instrucciones Globales para Copilot

> Estas instrucciones se aplican **automáticamente** a todas las interacciones de Copilot Chat en este proyecto.

---

## Proyecto

**BillMate** es una aplicación de facturación para pequeños negocios, construida como monorepo Maven con 4 microservicios Java 21 + Spring Boot 3.3.0. Proyecto TFG del ciclo de DAW.

| Servicio | Puerto | Descripción |
|---|---|---|
| API Gateway | 8080 | Entrada central, validación JWT reactiva (Spring Cloud Gateway) |
| Auth Service | 8081 | Registro y autenticación de usuarios con JWT |
| Billing Service | 8082 | Gestión de clientes, facturas y productos (Arquitectura Hexagonal) |
| Frontend Service | 8083 | Interfaz web (Spring Boot + Thymeleaf + vanilla JS) |
| Auth PostgreSQL | 5434 | Base de datos de autenticación (`auth_db`) |
| Billing PostgreSQL | 5433 | Base de datos de facturación (`billing_db`) |

---

## Stack Tecnológico

- **Java 21 (LTS)**, **Spring Boot 3.3.0**, **Spring Cloud 2023.0.3**
- **Spring Security + JWT** (jjwt 0.11.5, HS256, expiración 24h)
- **Spring Cloud Gateway** (reactivo, WebFlux)
- **Spring Data JPA + PostgreSQL 16**
- **OpenAPI Generator 7.3.0** (contract-first en billing-service)
- **iTextPDF 5.5.13.3** (generación de PDFs)
- **Thymeleaf** (frontend SSR) + **vanilla JavaScript**
- **Lombok** (todos los servicios EXCEPTO capa de dominio de billing)
- **Maven** (multi-módulo aggregator pom)
- **Docker** multi-stage builds (eclipse-temurin:21 Alpine)
- **GitHub Actions** (CI en PR a main, CD en tags semánticos)
- **Testcontainers** (PostgreSQL 16-alpine)
- **JUnit 5** + **Mockito** + **MockMvc**

---

## Reglas Universales de Código

### Idioma
- Código (clases, métodos, variables): **inglés**
- Mensajes de error al usuario: **español**
- Comentarios de código y documentación: **español**
- `@DisplayName` en tests: **inglés**

### Java
- Java 21: usar records para commands e inmutables, text blocks para strings multilínea
- **Inyección por constructor** siempre (NUNCA `@Autowired` en campos)
- `@RequiredArgsConstructor` (Lombok) en controllers y filtros
- Constructores explícitos en services de use case (billing)
- Variables en **camelCase**
- Paquete raíz: `com.billMate` (camelCase — mantener siempre esta convención)

### Arquitectura
- **Auth Service**: arquitectura por capas (Controller → Service → Repository)
- **Billing Service**: arquitectura hexagonal estricta (Ports & Adapters)
- **API Gateway**: reactivo (WebFlux)
- **Frontend**: Thymeleaf SSR + JS vanilla

### Errores
- Billing: `GlobalExceptionHandler` con `@RestControllerAdvice`, respuesta `ApiError`
- Mensajes constantes en `ErrorMessages.java`
- Formato estándar: `{ status, code, message, errors[], timestamp }`

### Testing
- JUnit 5 con `@DisplayName` descriptivo
- Billing dominio: fakes in-memory (NO Mockito)
- Billing controllers: `@WebMvcTest` + `@MockBean` + MockMvc
- Auth: integración con Testcontainers
- Perfiles de test: `application-test.yaml` con docker compose deshabilitado

### Git & CI/CD
- PRs a `main` disparan CI (`mvn clean verify`)
- Tags semánticos por servicio disparan CD (`auth-v1.0.0`, `billing-v1.0.0`, `gateway-v1.0.0`)
- Registry: GitHub Container Registry (GHCR)

---

## Convenciones de Nomenclatura

| Tipo | Patrón | Ejemplos |
|---|---|---|
| Modelos de dominio | `{Sustantivo}` | `Client`, `Invoice`, `InvoiceLineItem` |
| Use case (interfaz) | `{Verbo}{Sustantivo}UseCase` | `CreateClientUseCase`, `EmitInvoiceUseCase` |
| Use case (impl) | `{Verbo}{Sustantivo}Service` | `CreateClientService`, `EmitInvoiceService` |
| Commands (records) | `{Verbo}{Sustantivo}Command` | `CreateClientCommand`, `UpdateInvoiceCommand` |
| Puertos de salida | `{Sustantivo}RepositoryPort` / `{Sustantivo}Port` | `ClientRepositoryPort`, `PdfGeneratorPort` |
| Adaptadores JPA | `{Sustantivo}JpaAdapter` | `ClientJpaAdapter` |
| Entidades JPA | `{Sustantivo}Entity` | `ClientEntity`, `InvoiceEntity` |
| Repos Spring Data | `SpringData{Sustantivo}Repository` | `SpringDataClientRepository` |
| Mappers REST | `{Sustantivo}RestMapper` | `ClientRestMapper` |
| Mappers Persistencia | `{Sustantivo}PersistenceMapper` | `ClientPersistenceMapper` |
| Controllers | `{Sustantivo}Controller` | `ClientController`, `InvoiceController` |
| DTOs (billing) | `{Sustantivo}DTO`, `New{Sustantivo}DTO` | `ClientDTO`, `NewClientDTO` |
| DTOs (auth) | `{Sustantivo}Request/Response` | `LoginRequest`, `AuthResponse` |
| Método de use case | `execute()` | Un solo método por interfaz |
| Mappers REST | `toDto()`, `toCreateCommand()`, `toUpdateCommand()` | — |
| Mappers Persist. | `toDomain()`, `toEntity()` | — |

---

## Instrucciones Especializadas

Para contexto más detallado, Copilot carga automáticamente instrucciones especializadas según el archivo que estés editando:

| Archivo | Se activa cuando editas... |
|---|---|
| `billing-hexagonal.instructions.md` | `billing-service/**` |
| `auth-service.instructions.md` | `auth-service/**` |
| `api-gateway.instructions.md` | `api-gateway/**` |
| `frontend.instructions.md` | `frontend-service/**` |
| `testing.instructions.md` | `**/src/test/**` |
| `database.instructions.md` | `**/*.sql`, `**/entity/**`, `**/persistence/**` |
| `devops.instructions.md` | `**/Dockerfile`, `**/docker-compose*`, `.github/workflows/**` |

---

## Comandos Frecuentes

```bash
# Compilar todo
mvn clean install

# Tests de un servicio
cd billing-service && mvn clean verify

# Regenerar clases OpenAPI
cd billing-service && mvn clean compile

# Iniciar un servicio
cd auth-service && mvn spring-boot:run

# Base de datos
psql -U postgres -f scripts/create-tables.sql

# Docker Compose (desarrollo)
docker-compose -f auth-service/docker-compose.yaml up -d
docker-compose -f billing-service/docker-compose.yaml up -d
```
