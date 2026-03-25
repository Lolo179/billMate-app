# Billing Service – Arquitectura Hexagonal (Ports & Adapters)

## Regla Fundamental

La capa de **dominio** (`domain/`) es Java puro. **NUNCA** importar Spring, JPA, Lombok ni ningún framework. Las dependencias fluyen siempre hacia dentro: infraestructura → aplicación → dominio.

## Estructura

```
com.billMate.billing
├── domain/                              # JAVA PURO — sin frameworks
│   ├── shared/
│   │   └── PageResult.java             # Record genérico de paginación: items, page, size, totalElements, totalPages
│   ├── client/
│   │   ├── model/Client.java           # Validación en constructor + setters
│   │   └── port/
│   │       ├── in/                     # Use cases + Commands + Queries
│   │       │   ├── command/
│   │       │   │   ├── PatchClientCommand.java   # record con Optional<> por campo
│   │       │   │   └── CreateClientCommand.java, UpdateClientCommand.java
│   │       │   └── query/
│   │       │       └── ClientSearchQuery.java    # record (page, size, sortField, sortDir, name, nif)
│   │       └── out/ClientRepositoryPort.java  # search(ClientSearchQuery) → PageResult<Client>
│   └── invoice/
│       ├── model/
│       │   ├── Invoice.java            # recalculateTotal()
│       │   ├── InvoiceLineItem.java
│       │   └── InvoiceStatus.java      # DRAFT, SENT, PAID, CANCELLED
│       ├── event/InvoiceCreatedEvent.java  # Evento de dominio (record)
│       └── port/
│           ├── in/                     # Use cases + Commands + Queries
│           │   ├── command/
│           │   │   └── PatchInvoiceCommand.java  # record con Optional<> por campo + inner LineCommand
│           │   └── query/
│           │       └── InvoiceSearchQuery.java   # record (page, size, sortField, sortDir, status, dateFrom, dateTo, clientId)
│           └── out/
│               ├── InvoiceRepositoryPort.java  # search(InvoiceSearchQuery) → PageResult<Invoice>
│               ├── InvoiceEventPublisherPort.java
│               └── PdfGeneratorPort.java
├── application/useCase/                 # @Service, constructor explícito
│   ├── CreateClientService.java
│   ├── GetAllClientsService.java        # sort whitelist + delega en ClientSearchQuery
│   ├── GetAllInvoicesService.java       # sort whitelist + delega en InvoiceSearchQuery
│   ├── GetInvoicesByClientService.java  # valida que el cliente exista
│   ├── PatchClientService.java          # aplica Optional.ifPresent() por campo
│   ├── PatchInvoiceService.java         # valida DRAFT, aplica parches, recalcula total
│   └── ...                             # Un service por use case
└── infrastructure/
    ├── config/
    │   ├── JpaConfiguration.java
    │   ├── IdempotencyConfig.java       # Registra IdempotencyFilter
    │   └── WebConfig.java              # ShallowEtagHeaderFilter (ETag automático)
    ├── idempotency/
    │   ├── IdempotencyRecord.java       # record (status, body, contentType)
    │   ├── IdempotencyStore.java        # interfaz get/save
    │   ├── CaffeineIdempotencyStore.java # TTL 24h, máx 10.000 entradas
    │   └── IdempotencyFilter.java       # OncePerRequestFilter solo para POST
    ├── kafka/adapter/InvoiceKafkaAdapter.java  # @Async — implementa InvoiceEventPublisherPort
    ├── pdf/
    │   ├── StyledPdfGeneratorAdapter.java  # @Primary — PDF con diseño corporativo (activo)
    │   └── PdfGeneratorAdapter.java        # Implementación básica (fallback)
    ├── persistence/
    │   ├── adapter/                    # @Component — implementan puertos de salida
    │   ├── entity/                     # @Entity + Lombok
    │   ├── mapper/                     # @Component — toDomain(), toEntity()
    │   ├── repository/                 # Spring Data JPA + JpaSpecificationExecutor
    │   └── specification/              # ClientSpecifications, InvoiceSpecifications (filtros dinámicos)
    └── rest/
        ├── api/                        # Controllers (implementan interfaces OpenAPI)
        ├── dto/                        # ⚠️ GENERADOS por OpenAPI — NO EDITAR
        ├── error/                      # GlobalExceptionHandler + ErrorMessages
        └── mapper/                     # @Component — toDto(), toCreateCommand(), toSearchQuery(), toPatchCommand()
```

## Reglas por Capa

### Dominio (`domain/`)
- Java puro: **SIN Lombok, SIN JPA, SIN Spring**
- Modelos con validación en constructor + `validate()` privado
- Setters de campos obligatorios también validan
- Use cases: interfaz con un solo método `execute()`
- Commands: Java `record` con validación en compact constructor
- **Query records** en `port/in/query/`: transportan los parámetros de búsqueda/paginación al use case
  - `ClientSearchQuery(int page, int size, String sortField, String sortDir, String name, String nif)`
  - `InvoiceSearchQuery(int page, int size, String sortField, String sortDir, InvoiceStatus status, LocalDate dateFrom, LocalDate dateTo, Long clientId)`
- **Patch commands** en `port/in/command/` con `Optional<T>` por campo:
  - `null` en el DTO → `Optional.empty()` → el campo NO se actualiza
  - valor presente → `Optional.of(v)` → el campo SÍ se actualiza
- Eventos de dominio: Java `record` en `domain/*/event/` (ej: `InvoiceCreatedEvent`)
- Puertos de salida: interfaces que operan con modelos de dominio (nunca entidades JPA)
- `PageResult<T>`: record genérico en `domain/shared/` para respuestas paginadas — `(List<T> items, int page, int size, long totalElements, int totalPages)`
- **Puertos de búsqueda** usan query records: `search(ClientSearchQuery)` → `PageResult<Client>`, `search(InvoiceSearchQuery)` → `PageResult<Invoice>`

### Aplicación (`application/useCase/`)
- `@Service` de Spring
- Constructor **explícito** (no Lombok)
- Solo depende de puertos del dominio
- Implementa exactamente una interfaz de use case

### Infraestructura (`infrastructure/`)
- Lombok **sí** permitido: `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`
- Controllers: `@RestController` + `@RequiredArgsConstructor`, implementan interfaces OpenAPI
- Adaptadores JPA: `@Component` (no `@Repository`)
- Adaptador Kafka: `@Component` + `@Async` — publicación de eventos no bloquea el flujo principal
- Mappers: `@Component` manual (no MapStruct)
- Entidades: `@Entity` + `@Table(name = "...")` explícito
- PDF: `StyledPdfGeneratorAdapter` (`@Primary`) es la implementación activa — PDF con diseño corporativo (colores, tabla de líneas, sumas). `PdfGeneratorAdapter` permanece como fallback.

#### Especificaciones JPA (`persistence/specification/`)
- `ClientSpecifications.build(ClientSearchQuery)` → predicados ILIKE name + NIF exacto
- `InvoiceSpecifications.build(InvoiceSearchQuery)` → predicados clientId, status, dateFrom, dateTo
- Usados por los adaptadores JPA via `JpaSpecificationExecutor<T>`

#### Paginación en Adaptadores JPA (estrategia 2 pasos para Invoice)
Para evitar la advertencia Hibernate HHH90003004 con colecciones FETCH + paginación:
1. `findAll(spec, pageable)` — sin JOIN FETCH, para paginación limpia y count correcto
2. `findAllByInvoiceIdInWithLines(ids)` — con JOIN FETCH para cargar lines en memoria
3. Reordenar preservando el orden de la página

#### Filtros de Infraestructura (`infrastructure/idempotency/`)
- **Idempotencia HTTP**: `IdempotencyFilter extends OncePerRequestFilter` — solo aplica a POST con cabecera `Idempotency-Key`
  - Valida que el valor sea UUID bien formado → 400 si no
  - Si la clave existe en caché → reproduce la respuesta almacenada sin re-ejecutar el handler
  - Si la clave es nueva → ejecuta el handler, cachea respuestas 2xx con `ContentCachingResponseWrapper`
- **Cache**: `CaffeineIdempotencyStore` — TTL 24h, máximo 10.000 entradas (in-memory)
- **Registrado via**: `FilterRegistrationBean` en `IdempotencyConfig`

#### ETag (`infrastructure/config/WebConfig`)
- `ShallowEtagHeaderFilter` — añade cabecera `ETag` (MD5 del cuerpo) a todas las respuestas
- El cliente puede enviar `If-None-Match` para recibir `304 Not Modified` sin cuerpo
- No requiere cambios en controllers

## Contract-First (OpenAPI)

- Contrato: `contract/contract-billing.yaml` (OpenAPI 3.0.1)
- Plugin: `openapi-generator-maven-plugin 7.21.0`
- Genera interfaces API (`ClientsApi`, `InvoicesApi`) y DTOs
- Regenerar: `mvn clean compile`
- **NUNCA editar manualmente** archivos en `rest/dto/` ni interfaces `*Api`

### Endpoints REST

| Método | Ruta | Descripción | Notas |
|---|---|---|---|
| `GET` | `/clients` | Lista clientes paginada | `page`, `size`, `sort`, `name`, `nif` |
| `POST` | `/clients` | Crear cliente | `Idempotency-Key` header (UUID, opcional) |
| `GET` | `/clients/{id}` | Obtener cliente | — |
| `PUT` | `/clients/{id}` | Reemplazar cliente | — |
| `PATCH` | `/clients/{id}` | Actualización parcial | `application/merge-patch+json` |
| `DELETE` | `/clients/{id}` | Eliminar cliente | — |
| `GET` | `/invoices` | Lista facturas paginada | `page`, `size`, `sort`, `status`, `dateFrom`, `dateTo` |
| `POST` | `/invoices` | Crear factura | `Idempotency-Key` header (UUID, opcional) |
| `GET` | `/invoices/{id}` | Obtener factura | — |
| `PUT` | `/invoices/{id}` | Reemplazar factura | — |
| `PATCH` | `/invoices/{id}` | Actualización parcial | Solo facturas `DRAFT` |
| `DELETE` | `/invoices/{id}` | Eliminar factura | — |
| `GET` | `/invoices/client/{clientId}` | Facturas de un cliente | Mismos query params que GET /invoices |
| `PUT` | `/invoices/{id}/emit` | Emitir factura → PDF | `DRAFT → SENT` |
| `PUT` | `/invoices/{id}/pay` | Marcar como pagada | `SENT → PAID` |
| `GET` | `/invoices/{id}/pdf` | Descargar PDF | Solo `SENT` o `PAID` |

### Parámetro `sort`

Formato: `campo,direccion` (ej: `name,asc`, `createdAt,desc`). Validado contra whitelist en el service de aplicación → `IllegalArgumentException` → 400 si campo no permitido.

| Entidad | Campos permitidos |
|---|---|
| Client | `name`, `email`, `nif`, `createdAt` |
| Invoice | `date`, `total`, `status`, `createdAt` |

## Flujo de Datos en Controller

```
Request → DTO + params → RestMapper.toSearchQuery()/toPatchCommand()/toCreateCommand()
        → UseCase.execute(query/command)
        → Dominio
        → RestMapper.toDto()
        → Response
```

- HTTP 201 para creación, 200 para consultas/updates/patch, 204 para deletes

## Máquina de Estados de Facturas

```
DRAFT → SENT    (PUT /invoices/{id}/emit — genera PDF)
SENT  → PAID    (PUT /invoices/{id}/pay)
DRAFT → CANCELLED
```

- Solo `DRAFT` puede emitirse → `IllegalStateException` si no
- Solo `SENT` puede pagarse → `IllegalStateException` si no
- Solo `SENT` o `PAID` pueden descargar PDF

## Manejo de Errores

`GlobalExceptionHandler` (`@RestControllerAdvice`) con respuesta `ApiError`:

| Excepción | HTTP |
|---|---|
| `EntityNotFoundException` | 404 |
| `MethodArgumentNotValidException` | 400 |
| `ConstraintViolationException` | 400 |
| `HttpMessageNotReadableException` | 400 |
| `IllegalArgumentException` | 400 (sort inválido, estado inválido, UUID inválido) |
| `IllegalStateException` | 400 (transición de estado inválida) |
| `MaxUploadSizeExceededException` | 413 (payload > 1 MB) |
| `Exception` | 500 |

Mensajes constantes en `ErrorMessages.java` (en español). Límite de payload configurado en `application.yaml` (`max-request-size: 1MB`).

## Base de Datos

- PostgreSQL 16, puerto **5433**, base de datos `billing_db`
- Tablas: `clients`, `invoices`, `invoice_lines`
- Docker Compose: `billing-service/docker-compose.yaml`

## Testing – 3 Niveles

### Nivel 1: Tests de Dominio (Modelo y Commands)

**Ubicación:** `src/test/java/com/billMate/billing/domain/*/model/`

Solo JUnit 5 — **CERO mocks, CERO frameworks**.

```java
class ClientTest {
    private static final Long ID = 1L;
    private static final String VALID_NAME = "Acme Corp";
    // ... constantes private static final para datos de test

    private Client createValidClient() { ... } // helper

    @Nested
    @DisplayName("Constructor tests")
    class ConstructorTests { ... }

    @Nested
    @DisplayName("Name validation")
    class NameValidation { ... }
}
```

**Reglas:**
- `@Nested` para agrupar por escenario
- Constantes `private static final` para datos de test
- Helper `createValid{Entity}()` para crear instancias válidas
- Nombre: `shouldDoSomething` o `shouldThrowWhenX`
- Verificar constructor + setters + mensajes de excepción

### Nivel 2: Tests de Use Case (Fakes in-memory)

**Ubicación:** `src/test/java/com/billMate/billing/application/useCase/`

JUnit 5 + **fakes in-memory** (NO Mockito).

```java
class CreateClientServiceTest {
    private FakeClientRepository fakeRepository;
    private CreateClientService createClientService;

    @BeforeEach
    void setUp() {
        fakeRepository = new FakeClientRepository();
        createClientService = new CreateClientService(fakeRepository);
    }

    // Fake como clase interna private static que implementa el puerto de salida
    private static class FakeClientRepository implements ClientRepositoryPort {
        private final List<Client> savedClients = new ArrayList<>();
        private long idSequence = 1;
        private int saveCount = 0;
        // ... implementa save, findById, search, deleteById, existsById
        public int getSaveCount() { return saveCount; }

        @Override
        public PageResult<Client> search(ClientSearchQuery query) {
            int from = query.page() * query.size();
            int to = Math.min(from + query.size(), savedClients.size());
            List<Client> slice = from >= savedClients.size() ? List.of() : savedClients.subList(from, to);
            int totalPages = query.size() == 0 ? 0 : (int) Math.ceil((double) savedClients.size() / query.size());
            return new PageResult<>(slice, query.page(), query.size(), savedClients.size(), totalPages);
        }
    }
}
```

**Reglas:**
- Fake como `private static class` interna
- `@BeforeEach` crea nuevo fake + servicio (aislamiento)
- Nombre: `shouldDoSomething`

### Nivel 3: Tests de Controller (MockMvc)

**Ubicación:** `src/test/java/com/billMate/billing/infrastructure/rest/api/`

`@WebMvcTest` + `@MockitoBean` + MockMvc + Mockito.

```java
@WebMvcTest(controllers = ClientController.class)
public class ClientControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockitoBean private CreateClientUseCase createClientUseCase;
    @MockitoBean private PatchClientUseCase patchClientUseCase;  // ← incluir siempre
    @MockitoBean private ClientRestMapper clientRestMapper;
    // ... @MockitoBean para cada use case + mapper

    @Test
    void givenExistingClientId_whenGetClientById_thenReturnClientAndStatus200() throws Exception {
        // Given ... mock use case + mapper
        // When & Then
        mockMvc.perform(get("/clients/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Luis Alvarado"));
    }
}
```

**Reglas:**
- Nombre: `given{Contexto}_when{Acción}_then{Resultado}`
- `@MockitoBean` para **cada** use case inyectado en el controller (incluyendo `PatchClientUseCase` / `PatchInvoiceUseCase`)
- Si el controller llama a `mapper.toSearchQuery(...)`, mockear también ese método
- JSON bodies con text blocks (Java 21)
- Secciones: `// Given`, `// When & Then`

### Configuración de Test (`application-test.yaml`)

```yaml
spring:
  docker:
    compose:
      enabled: false
  jpa:
    hibernate:
      ddl-auto: create
  sql:
    init:
      mode: never
```

## Base de Datos

PostgreSQL 16, puerto **5433**, base de datos `billing_db`. Docker Compose: `billing-service/docker-compose.yaml`.

### Esquema

```sql
CREATE TABLE IF NOT EXISTS clients (
    client_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(255),
    nif VARCHAR(255),
    address VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS invoices (
    invoice_id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL,
    date DATE NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    description VARCHAR(255),
    total NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    tax_percentage NUMERIC(5, 2) NOT NULL DEFAULT 21.00,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_invoices_client FOREIGN KEY (client_id)
        REFERENCES clients(client_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS invoice_lines (
    id BIGSERIAL PRIMARY KEY,
    invoice_id BIGINT NOT NULL,
    description VARCHAR(100) NOT NULL,
    quantity NUMERIC(10, 2) NOT NULL,
    unit_price NUMERIC(10, 2) NOT NULL,
    total NUMERIC(10, 2) NOT NULL,
    CONSTRAINT fk_invoice_lines_invoice FOREIGN KEY (invoice_id)
        REFERENCES invoices(invoice_id) ON DELETE CASCADE
);
```

### Data Seeding (`data.sql`)

Se ejecuta en **cada arranque** (`sql.init.mode: always`). Hace `TRUNCATE` + reset de secuencias + inserta datos demo (~25 clientes, ~35 facturas). Solo en desarrollo — **nunca en tests** (`mode: never`).

- Puerto: **8082**

## Eventos (Kafka)

### Patrón de publicación

La publicación de eventos sigue el principio **fire-and-forget asíncrono**: el flujo principal (crear factura, etc.) no se bloquea si Kafka no está disponible.

- **Puerto de salida**: `InvoiceEventPublisherPort` (interfaz en dominio)
- **Evento**: `InvoiceCreatedEvent` (record en `domain/invoice/event/`)
- **Adaptador**: `InvoiceKafkaAdapter` (`@Component` + `@Async`) en `infrastructure/kafka/adapter/`
- **Topic**: `invoice.created`

### Resiliencia

- `CreateInvoiceService` envuelve la llamada al puerto de eventos en `try-catch` — un fallo de Kafka **nunca** impide la creación de la factura
- `InvoiceKafkaAdapter` es `@Async` y tiene su propio `try-catch` interno
- `@EnableAsync` habilitado en `BillServiceApplication`

### Configuración Kafka

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:29092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
```

Docker Compose: `kafka/docker-compose.yaml` (broker en `29092` para host, `9092` para contenedores; Kafka UI en `9090`).

## Observabilidad

### Correlation ID (`CorrelationIdFilter`)

`OncePerRequestFilter` con `@Order(HIGHEST_PRECEDENCE)` en `infrastructure/filter/`. Lee `x-Correlation-Id` del header (propagado por API Gateway) y lo coloca en MDC.

### Logging Estructurado

`logback-spring.xml` con `LogstashEncoder` (JSON). Todos los logs incluyen `correlationId` automáticamente desde MDC.

```java
import static net.logstash.logback.argument.StructuredArguments.kv;

// Controllers (INFO): entrada/salida de endpoints
log.info(">> POST /clients", kv("nif", newClientDTO.getNif()));
log.info("<< POST /clients", kv("clientId", created.getClientId()));

// Use Cases (INFO/DEBUG): lógica de negocio
log.info("Creating client", kv("nif", command.nif()), kv("name", command.name()));
log.warn("Client not found", kv("clientId", clientId));

// Adapters JPA (DEBUG): operaciones de persistencia
log.debug("Persisting client", kv("nif", client.getNif()));

// Exception Handlers (WARN/ERROR)
log.warn("Resource not found", kv("error", ex.getMessage()));
log.error("Unexpected error", kv("error", ex.getMessage()), ex);
```
