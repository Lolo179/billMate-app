---
applyTo: "**/src/test/**"
---

# Especialista: Testing – JUnit 5 + Fakes + MockMvc + Testcontainers

> Estas instrucciones se activan automáticamente al editar cualquier archivo dentro de `**/src/test/**`.

---

## Principios Generales

- **JUnit 5** en todos los servicios
- `@DisplayName` descriptivo en **inglés** en todos los tests
- Nombres de métodos de test en **camelCase** (no snake_case)
- Sin `public` en clases ni métodos de test (JUnit 5 no lo requiere)
- Perfiles de test: `application-test.yaml` con docker compose deshabilitado

---

## Billing Service – 3 Niveles de Test

### Nivel 1: Tests de Dominio (Modelo y Commands)

**Ubicación:** `billing-service/src/test/java/com/billMate/billing/domain/*/model/`

**Herramientas:** Solo JUnit 5 — **CERO mocks, CERO frameworks**

**Patrón:**

```java
class ClientTest {

    // Constantes para datos de test
    private static final Long ID = 1L;
    private static final String VALID_NAME = "Acme Corp";
    private static final String VALID_EMAIL = "contact@acme.com";
    private static final String VALID_PHONE = "+34 600 123 456";
    private static final String VALID_NIF = "B12345678";
    private static final String VALID_ADDRESS = "Calle Mayor 1, Madrid";
    private static final OffsetDateTime CREATED_AT = OffsetDateTime.now();

    // Método helper para crear instancia válida
    private Client createValidClient() {
        return new Client(ID, VALID_NAME, VALID_EMAIL, VALID_PHONE, VALID_NIF, VALID_ADDRESS, CREATED_AT);
    }

    @Nested
    @DisplayName("Constructor tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create client with all valid fields")
        void shouldCreateClientWithValidFields() {
            Client client = createValidClient();
            assertEquals(ID, client.getId());
            assertEquals(VALID_NAME, client.getName());
            // ...
        }
    }

    @Nested
    @DisplayName("Name validation")
    class NameValidation {

        @Test
        @DisplayName("Should throw when name is null")
        void shouldThrowWhenNameIsNull() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> new Client(ID, null, VALID_EMAIL, VALID_PHONE, VALID_NIF, VALID_ADDRESS, CREATED_AT));
            assertEquals("Client name is required", ex.getMessage());
        }

        @Test
        @DisplayName("Should throw when setting null name via setter")
        void shouldThrowWhenSetNameNull() {
            Client client = createValidClient();
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> client.setName(null));
            assertEquals("Client name is required", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("Setters for optional fields")
    class OptionalFieldSetters {

        @Test
        @DisplayName("Should allow null phone")
        void shouldAllowNullPhone() {
            Client client = createValidClient();
            client.setPhone(null);
            assertNull(client.getPhone());
        }
    }
}
```

**Reglas:**
- `@Nested` para agrupar por escenario (constructor, validación de nombre, email, NIF, setters opcionales)
- Constantes `private static final` para datos de test
- Método helper `createValid{Entity}()` para crear instancias válidas
- Nombre de método: `shouldDoSomething` o `shouldThrowWhenX`
- Verificar tanto constructor como setters
- Verificar mensajes de excepción con `assertEquals(expectedMessage, ex.getMessage())`

---

### Nivel 2: Tests de Use Case (Unitarios con Fakes)

**Ubicación:** `billing-service/src/test/java/com/billMate/billing/application/useCase/`

**Herramientas:** JUnit 5 + **fakes in-memory** (NO Mockito)

**Patrón:**

```java
class CreateClientServiceTest {

    private FakeClientRepository fakeRepository;
    private CreateClientService createClientService;

    @BeforeEach
    void setUp() {
        fakeRepository = new FakeClientRepository();
        createClientService = new CreateClientService(fakeRepository);
    }

    @Test
    @DisplayName("Should create client and delegate to repository port")
    void shouldCreateClientAndSave() {
        CreateClientCommand command = new CreateClientCommand(
                "Acme Corp", "contact@acme.com", "+34 600 123 456", "B12345678", "Calle Mayor 1"
        );

        Client result = createClientService.execute(command);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Acme Corp", result.getName());
        assertNotNull(result.getCreatedAt());
    }

    @Test
    @DisplayName("Should call repository save exactly once")
    void shouldCallSaveOnce() {
        CreateClientCommand command = new CreateClientCommand(
                "Empresa SA", "info@empresa.com", "+34 911 222 333", "C99999999", "Av. Principal 10"
        );

        createClientService.execute(command);

        assertEquals(1, fakeRepository.getSaveCount());
    }

    // FAKE in-memory — implementa el puerto de salida sin frameworks
    private static class FakeClientRepository implements ClientRepositoryPort {
        private final List<Client> savedClients = new ArrayList<>();
        private long idSequence = 1;
        private int saveCount = 0;

        @Override
        public Client save(Client client) {
            saveCount++;
            client.setId(idSequence++);
            savedClients.add(client);
            return client;
        }

        @Override
        public Optional<Client> findById(Long id) {
            return savedClients.stream().filter(c -> c.getId().equals(id)).findFirst();
        }

        @Override
        public List<Client> findAll() {
            return List.copyOf(savedClients);
        }

        @Override
        public void deleteById(Long id) {
            savedClients.removeIf(c -> c.getId().equals(id));
        }

        @Override
        public boolean existsById(Long id) {
            return savedClients.stream().anyMatch(c -> c.getId().equals(id));
        }

        public List<Client> getSavedClients() { return savedClients; }
        public int getSaveCount() { return saveCount; }
    }
}
```

**Reglas:**
- **Fake** como clase interna `private static` que implementa el puerto de salida
- El fake usa `ArrayList` + `idSequence` para simular persistencia
- Métodos de inspección: `getSavedClients()`, `getSaveCount()`
- `@BeforeEach` crea nuevo fake y servicio para cada test (aislamiento)
- **NO usar Mockito** — el propósito es probar hexagonal puro
- Nombre del método: `shouldDoSomething`

---

### Nivel 3: Tests de Controller (MockMvc)

**Ubicación:** `billing-service/src/test/java/com/billMate/billing/infrastructure/rest/api/`

**Herramientas:** `@WebMvcTest` + `@MockBean` + MockMvc + Mockito

**Patrón:**

```java
@WebMvcTest(controllers = ClientController.class)
public class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreateClientUseCase createClientUseCase;
    @MockBean
    private GetClientUseCase getClientUseCase;
    @MockBean
    private GetAllClientsUseCase getAllClientsUseCase;
    @MockBean
    private UpdateClientUseCase updateClientUseCase;
    @MockBean
    private DeleteClientUseCase deleteClientUseCase;
    @MockBean
    private ClientRestMapper clientRestMapper;

    @Test
    void givenExistingClientId_whenGetClientById_thenReturnClientAndStatus200() throws Exception {
        // Given
        Client mockClient = new Client(1L, "Luis Alvarado", "luis@gmail.com", null, "51246869s", "calle falsa 123", OffsetDateTime.now());
        when(getClientUseCase.execute(1L)).thenReturn(mockClient);

        ClientDTO mockDto = new ClientDTO();
        mockDto.setClientId(1L);
        mockDto.setName("Luis Alvarado");
        mockDto.setEmail("luis@gmail.com");
        mockDto.setNif("51246869s");
        when(clientRestMapper.toDto(mockClient)).thenReturn(mockDto);

        // When & Then
        mockMvc.perform(get("/clients/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Luis Alvarado"))
                .andExpect(jsonPath("$.email").value("luis@gmail.com"));
    }

    @Test
    void givenInvalidClientId_whenGetClientById_thenReturns404WithJson() throws Exception {
        // Given
        when(getClientUseCase.execute(999L))
                .thenThrow(new EntityNotFoundException("ID 999 no encontrado"));

        // When & Then
        mockMvc.perform(get("/clients/999"))
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value(ErrorMessages.RESOURCE_NOT_FOUND));
    }

    @Test
    void givenValidClient_whenPostClient_thenReturns201() throws Exception {
        // Given ... (mock mapper + use case)

        // When & Then
        mockMvc.perform(post("/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                    "name": "Ana Torres",
                    "email": "ana@example.com",
                    "nif": "12345678X",
                    "address": "Calle Real 123"
                }
            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Ana Torres"));
    }
}
```

**Reglas:**
- **Nombre del método:** `given{Contexto}_when{Acción}_then{Resultado}` (Given-When-Then)
- `@WebMvcTest(controllers = {Controller}.class)` — solo carga el controller específico
- `@MockBean` para cada use case + mapper del controller
- Se mockean tanto use cases como mappers (flujo completo)
- JSON bodies con **text blocks** (Java 21)
- Verificar status HTTP, JSON paths y mensajes de error
- Formato de secciones: `// Given`, `// When & Then` (combinados)

---

## Auth Service – Tests de Integración

**Ubicación:** `auth-service/src/test/java/com/billMate/auth/`

**Herramientas:** JUnit 5 + Testcontainers (PostgreSQL 16-alpine)

```java
@SpringBootTest
@ActiveProfiles("test")
public abstract class AuthIntegrationTestBase {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");
}
```

**Reglas:**
- Heredar de `AuthIntegrationTestBase`
- `@ActiveProfiles("test")` activa `application-test.yaml`
- Testcontainers con `@ServiceConnection` (auto-configura datasource)

---

## Configuración de Test (application-test.yaml)

### Billing Service:
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

### Auth Service:
```yaml
spring:
  docker:
    compose:
      enabled: false
  jpa:
    hibernate:
      ddl-auto: create-drop
  sql:
    init:
      mode: never
```

---

## Resumen de Convenciones

| Tipo de test | Patrón nombre método | Herramientas | Mocks |
|---|---|---|---|
| Dominio | `shouldDoSomething` | JUnit 5 puro | Ninguno |
| Use case | `shouldDoSomething` | JUnit 5 + fakes in-memory | Fake (NO Mockito) |
| Controller | `givenX_whenY_thenZ` | `@WebMvcTest` + MockMvc | `@MockBean` + Mockito |
| Integración | Descriptivo | Testcontainers | DB real en contenedor |

---

## Ejecutar Tests

```bash
# Billing service
cd billing-service && mvn clean verify

# Auth service
cd auth-service && mvn clean verify

# Gateway
cd api-gateway && mvn clean verify

# Todo el proyecto
mvn clean install
```
