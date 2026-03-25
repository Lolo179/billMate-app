# Billing Service – BillMate

Microservicio encargado de la gestión de clientes, facturas y productos dentro del sistema BillMate, implementado con **arquitectura hexagonal (Ports & Adapters)** y enfoque **contract-first** usando OpenAPI.

---

## 📄 Descripción

Este módulo contiene:

- La lógica de negocio de facturación, aislada en el dominio
- Puertos de entrada (use cases) y salida (repository ports) bien definidos
- Mappers dedicados por capa (REST y persistencia)
- El contrato OpenAPI (`contract-billing.yaml`) ubicado en `contract/`
- Generación automática de interfaces e instancias de modelo a partir del contrato
- Endpoints REST para gestión de clientes y facturas

---

## 🏛️ Arquitectura Hexagonal

El servicio sigue estrictamente la arquitectura hexagonal (Ports & Adapters), con una clara separación en tres capas:

### Dominio (`domain/`)

El núcleo de la aplicación, sin dependencias a frameworks ni infraestructura:

- **Modelos**: `Client`, `Invoice`, `InvoiceLineItem`, `InvoiceStatus`
- **Paginación**: `PageResult<T>` — record genérico en `domain/shared/` con `items`, `page`, `size`, `totalElements`, `totalPages`
- **Puertos de entrada (in)**: interfaces de casos de uso que definen las operaciones del sistema
  - `CreateClientUseCase`, `GetClientUseCase`, `GetAllClientsUseCase`, `UpdateClientUseCase`, `PatchClientUseCase`, `DeleteClientUseCase`
  - `CreateInvoiceUseCase`, `GetInvoiceUseCase`, `GetAllInvoicesUseCase`, `GetInvoicesByClientUseCase`, `UpdateInvoiceUseCase`, `PatchInvoiceUseCase`, `DeleteInvoiceUseCase`, `EmitInvoiceUseCase`, `DownloadInvoicePdfUseCase`, `PayInvoiceUseCase`
- **Commands**: `CreateClientCommand`, `UpdateClientCommand`, `PatchClientCommand` (Optional fields), `CreateInvoiceCommand`, `UpdateInvoiceCommand`, `PatchInvoiceCommand` (Optional fields)
- **Queries** (en `port/in/query/`): `ClientSearchQuery`, `InvoiceSearchQuery` — transportan parámetros de paginación, sort y filtros
- **Puertos de salida (out)**: `ClientRepositoryPort`, `InvoiceRepositoryPort`, `PdfGeneratorPort`, `InvoiceEventPublisherPort`

> Los puertos de búsqueda usan query objects: `search(ClientSearchQuery) → PageResult<Client>`, `search(InvoiceSearchQuery) → PageResult<Invoice>`.

### Aplicación (`application/useCase/`)

Implementaciones de los casos de uso, que orquestan la lógica de dominio:

- `CreateClientService`, `GetClientService`, `GetAllClientsService`, `UpdateClientService`, `PatchClientService`, `DeleteClientService`
- `CreateInvoiceService`, `GetInvoiceService`, `GetAllInvoicesService`, `GetInvoicesByClientService`, `UpdateInvoiceService`, `PatchInvoiceService`, `DeleteInvoiceService`, `EmitInvoiceService`, `DownloadInvoicePdfService`, `PayInvoiceService`

> Solo dependen de puertos de dominio, nunca de infraestructura.

### Infraestructura (`infrastructure/`)

Adaptadores que conectan el dominio con el mundo exterior:

- **REST** (`rest/api/`): `ClientController`, `InvoiceController` — implementan las interfaces OpenAPI generadas
- **Mappers REST** (`rest/mapper/`): `ClientRestMapper`, `InvoiceRestMapper` — convierten entre modelos de dominio y DTOs. Incluyen `toSearchQuery()` y `toPatchCommand()`
- **Persistencia** (`persistence/adapter/`): `ClientJpaAdapter`, `InvoiceJpaAdapter` — implementan los puertos de salida
- **Mappers Persistencia** (`persistence/mapper/`): `ClientPersistenceMapper`, `InvoicePersistenceMapper` — convierten entre modelos de dominio y entidades JPA
- **Especificaciones JPA** (`persistence/specification/`): `ClientSpecifications`, `InvoiceSpecifications` — filtros dinámicos para búsquedas
- **PDF** (`pdf/`): `StyledPdfGeneratorAdapter` (`@Primary`) — generador activo con diseño corporativo (colores, tabla de líneas); `PdfGeneratorAdapter` como fallback
- **Kafka** (`kafka/adapter/`): `InvoiceKafkaAdapter` (`@Async`) — implementa `InvoiceEventPublisherPort`
- **Idempotencia** (`idempotency/`): `IdempotencyFilter` + `CaffeineIdempotencyStore` — cacheado de respuestas POST
- **Config** (`config/`): `JpaConfiguration`, `WebConfig` (ETag via `ShallowEtagHeaderFilter`), `IdempotencyConfig` (registro del filtro)

### Diagrama de Dependencias

```
                    ┌──────────────────────────┐
                    │     REST Controllers      │
                    │  (ClientsApi, InvoicesApi) │
                    └────────────┬─────────────┘
                                 │ usa
                    ┌────────────▼─────────────┐
                    │     REST Mappers          │
                    │ (ClientRestMapper,        │
                    │  InvoiceRestMapper)        │
                    └────────────┬─────────────┘
                                 │ convierte a/desde
          ┌──────────────────────▼──────────────────────┐
          │              DOMINIO (núcleo)                │
          │  Modelos: Client, Invoice, InvoiceLineItem  │
          │  Puertos IN: *UseCase interfaces            │
          │  Puertos OUT: *RepositoryPort, PdfPort,     │
          │              InvoiceEventPublisherPort       │
          │  Commands: Create/Update*Command            │
          └──────────┬──────────────────┬───────────────┘
                     │                  │
        ┌────────────▼──────┐  ┌────────▼──────────────┐
        │  UseCase Services │  │ Persistence Adapters   │
        │  (application/)   │  │ + Persistence Mappers  │
        └───────────────────┘  └───────────────────────┘
```

---

## 🔧 Contract-First con OpenAPI

A partir del contrato `contract-billing.yaml`, se generan automáticamente:

- Interfaces de la API (`ClientsApi`, `InvoicesApi`)
- DTOs (`ClientDTO`, `InvoiceDTO`, `NewClientDTO`, `NewInvoiceDTO`, `InvoiceLine`)

> ⚠️ Estos archivos **no deben ser modificados manualmente** ni versionados en Git.

---

## 📁 Estructura del Proyecto

```
billing-service/
├── contract/
│   └── contract-billing.yaml          # Contrato OpenAPI
├── src/main/java/com/billMate/billing/
│   ├── domain/
│   │   ├── client/
│   │   │   ├── model/Client.java
│   │   │   └── port/
│   │   │       ├── in/
│   │   │       │   ├── command/       # CreateClientCommand, UpdateClientCommand, PatchClientCommand
│   │   │       │   └── query/        # ClientSearchQuery
│   │   │       └── out/               # ClientRepositoryPort
│   │   └── invoice/
│   │       ├── model/                 # Invoice, InvoiceLineItem, InvoiceStatus
│   │       ├── event/                 # InvoiceCreatedEvent (record)
│   │       └── port/
│   │           ├── in/
│   │           │   ├── command/       # CreateInvoiceCommand, UpdateInvoiceCommand, PatchInvoiceCommand
│   │           │   └── query/        # InvoiceSearchQuery
│   │           └── out/               # InvoiceRepositoryPort, PdfGeneratorPort, InvoiceEventPublisherPort
│   └── shared/
│       └── PageResult.java            # Record genérico de paginación (items, page, size, totalElements, totalPages)
│   ├── application/
│   │   └── useCase/                   # Implementaciones de use cases
│   └── infrastructure/
│       ├── config/
│       │   ├── JpaConfiguration.java
│       │   ├── IdempotencyConfig.java  # Registra IdempotencyFilter (POST /clients, /invoices)
│       │   └── WebConfig.java         # ShallowEtagHeaderFilter (ETag automático en GET)
│       ├── idempotency/               # IdempotencyRecord, IdempotencyStore, CaffeineIdempotencyStore, IdempotencyFilter
│       ├── kafka/adapter/             # InvoiceKafkaAdapter (@Async)
│       ├── pdf/                       # StyledPdfGeneratorAdapter (@Primary), PdfGeneratorAdapter
│       ├── persistence/
│       │   ├── adapter/               # ClientJpaAdapter, InvoiceJpaAdapter
│       │   ├── entity/                # ClientEntity, InvoiceEntity, InvoiceLineEntity
│       │   ├── mapper/                # ClientPersistenceMapper, InvoicePersistenceMapper
│       │   ├── repository/            # SpringDataClientRepository, SpringDataInvoiceRepository
│       │   └── specification/         # ClientSpecifications, InvoiceSpecifications (filtros dinámicos)
│       └── rest/
│           ├── api/                   # ClientController, InvoiceController
│           ├── dto/                   # DTOs generados por OpenAPI
│           ├── error/                 # GlobalExceptionHandler, ErrorMessages
│           └── mapper/                # ClientRestMapper, InvoiceRestMapper
└── src/test/java/com/billMate/billing/
    ├── application/useCase/           # Tests unitarios de use cases
    ├── domain/client/model/           # Tests de validación de dominio
    └── infrastructure/rest/api/       # Tests de controllers
```

---

## 🛠️ Stack Tecnológico

- Java 21 (LTS)
- Spring Boot 4.0.4
- Spring Data JPA + JPA Specifications
- PostgreSQL 16
- Apache Kafka 3.8.0 (KRaft) + Spring Kafka
- OpenAPI Generator 7.21.0 (contract-first)
- iText 5.5.13.3 (generación de PDF)
- Caffeine (caché in-memory para idempotencia)
- Maven
- Lombok

---

## 🔧 Configuración por Defecto

El servicio se levanta en el puerto:

```
http://localhost:8082
```

Y utiliza la base de datos PostgreSQL `billing_db` (puerto 5433). Puedes ajustar estos valores desde el archivo:

```
src/main/resources/application.yaml
```

---

## � Principios REST Implementados

### Paginación con Sort + Filtros (`GET /clients`, `GET /invoices`)

Parámetros de query soportados:

| Param | Endpoints | Descripción |
|---|---|---|
| `page` | todos los GET paginados | Número de página (0-indexed) |
| `size` | todos los GET paginados | Tamaño de página |
| `sort` | todos los GET paginados | `campo,direccion` (ej: `name,asc`, `createdAt,desc`) |
| `name` | `GET /clients` | Filtro por nombre (contiene, case-insensitive) |
| `nif` | `GET /clients` | Filtro por NIF exacto |
| `status` | `GET /invoices`, `GET /invoices/client/{id}` | Filtro por estado (`DRAFT`, `SENT`, `PAID`, `CANCELLED`) |
| `dateFrom` | `GET /invoices`, `GET /invoices/client/{id}` | Filtro por fecha desde (`yyyy-MM-dd`) |
| `dateTo` | `GET /invoices`, `GET /invoices/client/{id}` | Filtro por fecha hasta (`yyyy-MM-dd`) |

El parámetro `sort` se valida contra una whitelist en el service de aplicación — campo inválido → `IllegalArgumentException` → 400.

### PATCH — Actualización Parcial (RFC 7396)

- `PATCH /clients/{id}` — actualiza solo los campos presentes en el body
- `PATCH /invoices/{id}` — solo permitido en facturas en estado `DRAFT`
- Semántica: `null` o campo ausente = no modificar; valor presente = actualizar

```json
// Ejemplo: solo cambiar el nombre del cliente
PATCH /clients/1
Content-Type: application/merge-patch+json

{ "name": "Nuevo Nombre" }
```

### Idempotencia en POST

Header opcional en `POST /clients` y `POST /invoices`:

```
Idempotency-Key: 550e8400-e29b-41d4-a716-446655440000
```

- UUID bien formado → 400 si el formato es inválido
- Si la clave ya existe en caché → reproduce la respuesta original sin re-ejecutar la lógica
- Si es nueva → ejecuta normalmente y cachea la respuesta 2xx
- **Caché**: Caffeine, TTL 24 horas, máximo 10.000 entradas (in-memory)

### ETag en GET

- `ShallowEtagHeaderFilter` añade automáticamente la cabecera `ETag` (MD5 del body) a todas las respuestas
- El cliente puede usar `If-None-Match: "<etag>"` para recibir `304 Not Modified` sin cuerpo
- No requiere cambios en los controllers

### Límite de Payload

- Máximo 1 MB por request (`spring.servlet.multipart.max-request-size: 1MB`)
- Exceder el límite → `MaxUploadSizeExceededException` → 413 en `GlobalExceptionHandler`

---

## �🚀 Compilar y Generar Clases desde el Contrato

Para generar las clases desde el contrato OpenAPI:

```bash
cd billing-service
mvn clean install
```

Esto ejecutará el plugin `openapi-generator-maven-plugin` y generará automáticamente las clases.

---

## 📊 CI/CD

### CI — `.github/workflows/billing-ci.yaml`

- **Trigger**: PR a `main`
- **Acciones**:
  - ✅ Ejecución de tests (`mvn clean verify`) con Java 21 y cache Maven
  - ✅ Build de imagen Docker (sin push al registro)

### CD — `.github/workflows/deploy.yaml` (pipeline global)

El deploy está centralizado en un único workflow a nivel de monorepo que se activa con cada push a `main`:
- Construye y publica la imagen `ghcr.io/{owner}/billing-service:latest` en GHCR
- Ejecuta las pruebas E2E Playwright en paralelo
- Despliega a EC2 solo si ambos pasos pasan

---

## 📋 Contrato OpenAPI

El contrato de la API se encuentra en:

```
contract/contract-billing.yaml
```

Puedes visualizarlo directamente en Swagger Editor:

[![Ver en Swagger Editor](https://img.shields.io/badge/Swagger--UI-View%20Contract-green?logo=swagger)](https://editor.swagger.io/?url=https://raw.githubusercontent.com/Lolo179/billMate-app/main/billing-service/contract/contract-billing.yaml)

O acceder a Swagger UI cuando el servicio esté corriendo:

```
http://localhost:8082/swagger-ui.html
```

---

## 🧪 Testing

Para ejecutar los tests del servicio:

```bash
cd billing-service
mvn clean verify
```

Los tests están organizados por capa siguiendo la arquitectura hexagonal:

```
src/test/java/com/billMate/billing/
├── application/useCase/           # Tests unitarios de use cases
├── domain/client/model/           # Tests de validación del modelo de dominio
└── infrastructure/rest/api/       # Tests de controllers (MockMvc + @WebMvcTest)
```

---

## � Eventos (Kafka)

Al crear una factura, se publica un evento `InvoiceCreatedEvent` en el topic `invoice.created` de Kafka. La publicación es **asíncrona** (`@Async`) y **no bloquea** el flujo principal — si Kafka no está disponible, la factura se crea igualmente.

- **Broker**: `localhost:29092` (host) / `kafka:9092` (contenedores)

---

## 📈 Observabilidad

- **Correlation ID** (`CorrelationIdFilter`): lee el header `x-Correlation-Id` propagado por el API Gateway y lo coloca en MDC.
- **Logs JSON estructurados**: `logback-spring.xml` con `LogstashEncoder` (logstash-logback-encoder 8.1). Compatible con el stack Grafana + Loki + Promtail.

```bash
# Levantar stack de observabilidad
docker-compose -f ../observability/docker-compose.yaml up -d   # Grafana en http://localhost:3000
```

Query LogQL de ejemplo en Grafana: `{service="billing"} |= "invoice"`
- **Kafka UI**: `http://localhost:9090`
- **Docker Compose**: `kafka/docker-compose.yaml`

---

## �🐳 Docker

Para construir la imagen Docker:

```bash
docker build -t billmate/billing-service:latest .
```

Para ejecutar con docker-compose:

```bash
docker-compose up -d
```

---

## 🔐 Seguridad

- Los endpoints de este servicio están protegidos por JWT
- El token debe ser validado a través del **API Gateway**
- Las peticiones deben incluir el header:

```
Authorization: Bearer <tu-token-jwt>
```

---

## 📚 Referencias

- [BillMate Principal README](../README.md)
- [Auth Service](../auth-service/README.md)
- [API Gateway](../api-gateway/README.md)
- [Database Setup](../scripts/README-DATABASE.md)

