# Billing Service – Arquitectura Hexagonal (Ports & Adapters)

## Regla Fundamental

La capa de **dominio** (`domain/`) es Java puro. **NUNCA** importar Spring, JPA, Lombok ni ningún framework. Las dependencias fluyen siempre hacia dentro: infraestructura → aplicación → dominio.

## Estructura

```
com.billMate.billing
├── domain/                              # JAVA PURO — sin frameworks
│   ├── client/
│   │   ├── model/Client.java           # Validación en constructor + setters
│   │   └── port/
│   │       ├── in/                     # Use cases + Commands (records)
│   │       └── out/ClientRepositoryPort.java
│   └── invoice/
│       ├── model/
│       │   ├── Invoice.java            # recalculateTotal()
│       │   ├── InvoiceLineItem.java
│       │   └── InvoiceStatus.java      # DRAFT, SENT, PAID, CANCELLED
│       └── port/
│           ├── in/                     # Use cases + Commands
│           └── out/
│               ├── InvoiceRepositoryPort.java
│               └── PdfGeneratorPort.java
├── application/useCase/                 # @Service, constructor explícito
│   ├── CreateClientService.java
│   └── ...                             # Un service por use case
└── infrastructure/
    ├── config/JpaConfiguration.java
    ├── pdf/PdfGeneratorAdapter.java
    ├── persistence/
    │   ├── adapter/                    # @Component — implementan puertos de salida
    │   ├── entity/                     # @Entity + Lombok
    │   ├── mapper/                     # @Component — toDomain(), toEntity()
    │   └── repository/                 # Spring Data JPA
    └── rest/
        ├── api/                        # Controllers (implementan interfaces OpenAPI)
        ├── dto/                        # ⚠️ GENERADOS por OpenAPI — NO EDITAR
        ├── error/                      # GlobalExceptionHandler + ErrorMessages
        └── mapper/                     # @Component — toDto(), toCreateCommand()
```

## Reglas por Capa

### Dominio (`domain/`)
- Java puro: **SIN Lombok, SIN JPA, SIN Spring**
- Modelos con validación en constructor + `validate()` privado
- Setters de campos obligatorios también validan
- Use cases: interfaz con un solo método `execute()`
- Commands: Java `record` con validación en compact constructor
- Puertos de salida: interfaces que operan con modelos de dominio (nunca entidades JPA)

### Aplicación (`application/useCase/`)
- `@Service` de Spring
- Constructor **explícito** (no Lombok)
- Solo depende de puertos del dominio
- Implementa exactamente una interfaz de use case

### Infraestructura (`infrastructure/`)
- Lombok **sí** permitido: `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`
- Controllers: `@RestController` + `@RequiredArgsConstructor`, implementan interfaces OpenAPI
- Adaptadores JPA: `@Component` (no `@Repository`)
- Mappers: `@Component` manual (no MapStruct)
- Entidades: `@Entity` + `@Table(name = "...")` explícito

## Contract-First (OpenAPI)

- Contrato: `contract/contract-billing.yaml` (OpenAPI 3.0.1)
- Plugin: `openapi-generator-maven-plugin 7.3.0`
- Genera interfaces API (`ClientsApi`, `InvoicesApi`) y DTOs
- Regenerar: `mvn clean compile`
- **NUNCA editar manualmente** archivos en `rest/dto/` ni interfaces `*Api`

## Flujo de Datos en Controller

```
Request → DTO → RestMapper.toCommand() → UseCase.execute() → Dominio → RestMapper.toDto() → Response
```

- HTTP 201 para creación, 200 para consultas/updates, 204 para deletes

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
| `IllegalStateException` | 400 |
| `Exception` | 500 |

Mensajes constantes en `ErrorMessages.java` (en español).

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
        // ... implementa save, findById, findAll, deleteById, existsById
        public int getSaveCount() { return saveCount; }
    }
}
```

**Reglas:**
- Fake como `private static class` interna
- `@BeforeEach` crea nuevo fake + servicio (aislamiento)
- Nombre: `shouldDoSomething`

### Nivel 3: Tests de Controller (MockMvc)

**Ubicación:** `src/test/java/com/billMate/billing/infrastructure/rest/api/`

`@WebMvcTest` + `@MockBean` + MockMvc + Mockito.

```java
@WebMvcTest(controllers = ClientController.class)
public class ClientControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockBean private CreateClientUseCase createClientUseCase;
    @MockBean private ClientRestMapper clientRestMapper;
    // ... @MockBean para cada use case + mapper

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
- `@MockBean` para cada use case + mapper
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
