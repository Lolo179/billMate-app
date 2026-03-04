---
applyTo: "billing-service/**"
---

# Especialista: Billing Service – Arquitectura Hexagonal (Ports & Adapters)

> Estas instrucciones se activan automáticamente al editar cualquier archivo dentro de `billing-service/`.

---

## Regla Fundamental

La capa de **dominio** (`domain/`) es el núcleo de la aplicación. **NUNCA** debe importar clases de Spring, JPA, Lombok, ni ningún otro framework. Las dependencias fluyen siempre hacia dentro (infraestructura → aplicación → dominio).

---

## Estructura del Proyecto

```
com.billMate.billing
├── BillServiceApplication.java
├── domain/                                  # CAPA DE DOMINIO (Java puro)
│   ├── client/
│   │   ├── model/Client.java               # Modelo con validación en constructor + setters
│   │   └── port/
│   │       ├── in/                          # Puertos de entrada
│   │       │   ├── CreateClientUseCase.java # Interfaz con execute()
│   │       │   ├── CreateClientCommand.java # Record con validación
│   │       │   ├── GetClientUseCase.java
│   │       │   ├── GetAllClientsUseCase.java
│   │       │   ├── UpdateClientUseCase.java
│   │       │   ├── UpdateClientCommand.java
│   │       │   └── DeleteClientUseCase.java
│   │       └── out/                         # Puertos de salida
│   │           └── ClientRepositoryPort.java
│   └── invoice/
│       ├── model/
│       │   ├── Invoice.java                 # Modelo con recalculateTotal()
│       │   ├── InvoiceLineItem.java         # Modelo con validación
│       │   └── InvoiceStatus.java           # Enum: DRAFT, SENT, PAID, CANCELLED
│       └── port/
│           ├── in/                          # Use cases + Commands
│           └── out/
│               ├── InvoiceRepositoryPort.java
│               └── PdfGeneratorPort.java
├── application/useCase/                     # CAPA DE APLICACIÓN (orquestación)
│   ├── CreateClientService.java             # @Service, constructor explícito
│   ├── GetClientService.java
│   ├── ...                                  # Un service por use case
├── infrastructure/                          # CAPA DE INFRAESTRUCTURA (adaptadores)
│   ├── config/JpaConfiguration.java
│   ├── pdf/PdfGeneratorAdapter.java         # Implementa PdfGeneratorPort
│   ├── persistence/
│   │   ├── adapter/                         # Implementan puertos de salida
│   │   │   ├── ClientJpaAdapter.java        # @Component
│   │   │   └── InvoiceJpaAdapter.java
│   │   ├── entity/                          # Entidades JPA (@Entity, Lombok)
│   │   │   ├── ClientEntity.java
│   │   │   ├── InvoiceEntity.java
│   │   │   └── InvoiceLineEntity.java
│   │   ├── mapper/                          # Mappers dominio ↔ entidad (@Component)
│   │   │   ├── ClientPersistenceMapper.java
│   │   │   └── InvoicePersistenceMapper.java
│   │   └── repository/                      # Spring Data JPA
│   │       ├── SpringDataClientRepository.java
│   │       └── SpringDataInvoiceRepository.java
│   └── rest/
│       ├── api/                             # Controllers (implementan interfaces OpenAPI)
│       │   ├── ClientController.java        # @RestController @RequiredArgsConstructor
│       │   └── InvoiceController.java
│       ├── dto/                             # ⚠️ GENERADOS por OpenAPI - NO EDITAR
│       ├── error/
│       │   ├── GlobalExceptionHandler.java  # @RestControllerAdvice
│       │   └── ErrorMessages.java           # Constantes de mensajes
│       └── mapper/                          # Mappers dominio ↔ DTO (@Component)
│           ├── ClientRestMapper.java
│           └── InvoiceRestMapper.java
```

---

## Patrones de Código con Ejemplos Reales

### 1. Interfaz de Use Case (Puerto de Entrada)

```java
package com.billMate.billing.domain.client.port.in;

import com.billMate.billing.domain.client.model.Client;

public interface CreateClientUseCase {
    Client execute(CreateClientCommand command);
}
```

**Reglas:**
- Un método `execute()` por interfaz (Single Responsibility)
- Recibe un Command o un tipo simple (Long id)
- Retorna el modelo de dominio
- Sin anotaciones de framework

### 2. Command (Record con Validación)

```java
package com.billMate.billing.domain.client.port.in;

public record CreateClientCommand(
        String name,
        String email,
        String phone,
        String nif,
        String address
) {
    public CreateClientCommand {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Client name is required");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Client email is required");
        }
        if (nif == null || nif.isBlank()) {
            throw new IllegalArgumentException("Client NIF is required");
        }
    }
}
```

**Reglas:**
- Siempre Java `record` (inmutable)
- Validación en compact constructor
- Lanza `IllegalArgumentException` con mensaje descriptivo en inglés
- Sin anotaciones de framework

### 3. Modelo de Dominio

```java
package com.billMate.billing.domain.client.model;

import java.time.OffsetDateTime;

public class Client {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private String nif;
    private String address;
    private OffsetDateTime createdAt;

    public Client() {}

    public Client(Long id, String name, String email, String phone, String nif, String address, OffsetDateTime createdAt) {
        validate(name, email, nif);
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.nif = nif;
        this.address = address;
        this.createdAt = createdAt;
    }

    private void validate(String name, String email, String nif) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Client name is required");
        }
        if (name.length() > 255) {
            throw new IllegalArgumentException("Client name must not exceed 255 characters");
        }
        // ... más validaciones
    }

    // Getters con validación en setters para campos obligatorios
    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Client name is required");
        }
        if (name.length() > 255) {
            throw new IllegalArgumentException("Client name must not exceed 255 characters");
        }
        this.name = name;
    }
    // ... resto de getters/setters
}
```

**Reglas:**
- Clase Java pura — **SIN Lombok, SIN anotaciones JPA, SIN Spring**
- Validación en constructor + método `validate()` privado
- Setters de campos obligatorios también validan
- Getters/setters manuales (no se usa Lombok en dominio)
- Usa `OffsetDateTime` para timestamps

### 4. Puerto de Salida (Repository Port)

```java
package com.billMate.billing.domain.client.port.out;

import com.billMate.billing.domain.client.model.Client;
import java.util.List;
import java.util.Optional;

public interface ClientRepositoryPort {
    Client save(Client client);
    Optional<Client> findById(Long id);
    List<Client> findAll();
    void deleteById(Long id);
    boolean existsById(Long id);
}
```

**Reglas:**
- Interfaz Java pura
- Opera con modelos de dominio (nunca entidades JPA)
- Métodos: `save()`, `findById()`, `findAll()`, `deleteById()`, `existsById()`

### 5. Implementación de Use Case (Application Layer)

```java
package com.billMate.billing.application.useCase;

import com.billMate.billing.domain.client.model.Client;
import com.billMate.billing.domain.client.port.in.CreateClientCommand;
import com.billMate.billing.domain.client.port.in.CreateClientUseCase;
import com.billMate.billing.domain.client.port.out.ClientRepositoryPort;
import org.springframework.stereotype.Service;
import java.time.OffsetDateTime;

@Service
public class CreateClientService implements CreateClientUseCase {

    private final ClientRepositoryPort clientRepositoryPort;

    // Constructor explícito (NO @RequiredArgsConstructor en use cases)
    public CreateClientService(ClientRepositoryPort clientRepositoryPort) {
        this.clientRepositoryPort = clientRepositoryPort;
    }

    @Override
    public Client execute(CreateClientCommand command) {
        Client client = new Client(
                null,
                command.name(),
                command.email(),
                command.phone(),
                command.nif(),
                command.address(),
                OffsetDateTime.now()
        );
        return clientRepositoryPort.save(client);
    }
}
```

**Reglas:**
- `@Service` de Spring
- Constructor **explícito** (no Lombok en use cases)
- Solo depende de puertos del dominio (nunca de infraestructura)
- Implementa exactamente una interfaz de use case

### 6. Entidad JPA

```java
package com.billMate.billing.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "clients")
public class ClientEntity {
    @Id
    @Column(name = "client_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String phone;
    private String nif;
    private String address;
    private OffsetDateTime createdAt;
}
```

**Reglas:**
- Lombok sí permitido en infraestructura: `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`
- `@Table(name = "tabla")` explícito
- PKs con `@GeneratedValue(strategy = GenerationType.IDENTITY)`
- Nombres de columna: snake_case (si difiere, usar `@Column(name = "...")`)

### 7. Adaptador JPA (Puerto de Salida)

```java
package com.billMate.billing.infrastructure.persistence.adapter;

import com.billMate.billing.domain.client.model.Client;
import com.billMate.billing.domain.client.port.out.ClientRepositoryPort;
import com.billMate.billing.infrastructure.persistence.mapper.ClientPersistenceMapper;
import com.billMate.billing.infrastructure.persistence.repository.SpringDataClientRepository;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ClientJpaAdapter implements ClientRepositoryPort {

    private final SpringDataClientRepository springDataClientRepository;
    private final ClientPersistenceMapper clientPersistenceMapper;

    public ClientJpaAdapter(SpringDataClientRepository springDataClientRepository,
                            ClientPersistenceMapper clientPersistenceMapper) {
        this.springDataClientRepository = springDataClientRepository;
        this.clientPersistenceMapper = clientPersistenceMapper;
    }

    @Override
    public Client save(Client client) {
        ClientEntity entity = clientPersistenceMapper.toEntity(client);
        ClientEntity saved = springDataClientRepository.save(entity);
        return clientPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<Client> findById(Long id) {
        return springDataClientRepository.findById(id).map(clientPersistenceMapper::toDomain);
    }

    @Override
    public List<Client> findAll() {
        return springDataClientRepository.findAll().stream()
                .map(clientPersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        springDataClientRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return springDataClientRepository.existsById(id);
    }
}
```

**Reglas:**
- `@Component` (no `@Repository`, eso es para Spring Data)
- Implementa el puerto de salida del dominio
- Usa el mapper de persistencia para convertir dominio ↔ entidad
- Constructor explícito

### 8. Mapper de Persistencia

```java
@Component
public class ClientPersistenceMapper {

    public ClientEntity toEntity(Client client) {
        return ClientEntity.builder()
                .id(client.getId())
                .name(client.getName())
                .email(client.getEmail())
                .phone(client.getPhone())
                .nif(client.getNif())
                .address(client.getAddress())
                .createdAt(client.getCreatedAt())
                .build();
    }

    public Client toDomain(ClientEntity entity) {
        return new Client(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getPhone(),
                entity.getNif(),
                entity.getAddress(),
                entity.getCreatedAt()
        );
    }
}
```

**Reglas:**
- `@Component` manual (no MapStruct)
- Métodos: `toEntity()` (usa builder), `toDomain()` (usa constructor)

### 9. Mapper REST

```java
@Component
public class ClientRestMapper {

    public ClientDTO toDto(Client client) {
        ClientDTO dto = new ClientDTO();
        dto.setClientId(client.getId());
        dto.setName(client.getName());
        dto.setEmail(client.getEmail());
        dto.setPhone(client.getPhone());
        dto.setNif(client.getNif());
        dto.setAddress(client.getAddress());
        dto.setCreatedAt(client.getCreatedAt());
        return dto;
    }

    public CreateClientCommand toCreateCommand(NewClientDTO dto) {
        return new CreateClientCommand(
                dto.getName(), dto.getEmail(), dto.getPhone(), dto.getNif(), dto.getAddress()
        );
    }

    public UpdateClientCommand toUpdateCommand(Long clientId, NewClientDTO dto) {
        return new UpdateClientCommand(
                clientId, dto.getName(), dto.getEmail(), dto.getPhone(), dto.getNif(), dto.getAddress()
        );
    }
}
```

**Reglas:**
- `@Component` manual
- `toDto()`: dominio → DTO (usa setters del DTO generado)
- `toCreateCommand()` / `toUpdateCommand()`: DTO → Command

### 10. Controller

```java
@RestController
@RequiredArgsConstructor
public class ClientController implements ClientsApi {

    private final CreateClientUseCase createClientUseCase;
    private final GetClientUseCase getClientUseCase;
    private final GetAllClientsUseCase getAllClientsUseCase;
    private final UpdateClientUseCase updateClientUseCase;
    private final DeleteClientUseCase deleteClientUseCase;
    private final ClientRestMapper clientRestMapper;

    @Override
    public ResponseEntity<ClientDTO> createClient(NewClientDTO newClientDTO) {
        CreateClientCommand command = clientRestMapper.toCreateCommand(newClientDTO);
        Client client = createClientUseCase.execute(command);
        return ResponseEntity.status(201).body(clientRestMapper.toDto(client));
    }

    @Override
    public ResponseEntity<Void> deleteClient(Long clientId) {
        deleteClientUseCase.execute(clientId);
        return ResponseEntity.noContent().build();
    }
    // ... más overrides
}
```

**Reglas:**
- `@RestController` + `@RequiredArgsConstructor` (Lombok SÍ en controllers)
- Implementa la interfaz generada por OpenAPI (`ClientsApi`, `InvoicesApi`)
- Inyecta use cases individuales (no un servicio monolítico)
- Inyecta el REST mapper
- Flujo: DTO → Command (vía mapper) → UseCase.execute() → Dominio → DTO (vía mapper)
- HTTP 201 para creación, 200 para consultas/updates, 204 para deletes

---

## Manejo de Errores

### GlobalExceptionHandler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiError> handleEntityNotFound(EntityNotFoundException ex) {
        ApiError error = new ApiError()
                .status(HttpStatus.NOT_FOUND.name())
                .code(HttpStatus.NOT_FOUND.value())
                .message(ErrorMessages.RESOURCE_NOT_FOUND)
                .errors(List.of(ex.getMessage()))
                .timestamp(OffsetDateTime.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    // ... más handlers
}
```

| Excepción | HTTP | Handler |
|---|---|---|
| `EntityNotFoundException` | 404 | `handleEntityNotFound` |
| `MethodArgumentNotValidException` | 400 | `handleValidation` |
| `ConstraintViolationException` | 400 | `handleConstraintViolation` |
| `HttpMessageNotReadableException` | 400 | `handleInvalidJson` |
| `IllegalStateException` | 400 | `handleIllegalState` |
| `Exception` | 500 | `handleUnexpected` |

### ErrorMessages (constantes)

```java
public class ErrorMessages {
    public static final String RESOURCE_NOT_FOUND = "Recurso no encontrado con el ID especificado.";
    public static final String CLIENT_NOT_FOUND = "Cliente no encontrado con el ID especificado.";
    public static final String INVOICE_NOT_FOUND = "Factura no encontrada con el ID especificado.";
    public static final String VALIDATION_FAILED = "La solicitud contiene errores de validación.";
    public static final String CONSTRAINT_VIOLATION = "Violaciones de restricciones en los datos de entrada.";
    public static final String INVALID_JSON = "Formato JSON inválido o mal formado.";
    public static final String UNEXPECTED_ERROR = "Ha ocurrido un error inesperado en el servidor.";
}
```

---

## Máquina de Estados de Facturas

```
DRAFT → SENT   (vía PUT /invoices/{id}/emit — genera PDF)
SENT  → PAID   (vía PUT /invoices/{id}/pay)
DRAFT → CANCELLED (manual)
```

- Solo facturas en `DRAFT` pueden ser emitidas → lanza `IllegalStateException` si no
- Solo facturas en `SENT` pueden ser pagadas → lanza `IllegalStateException` si no
- Solo facturas en `SENT` o `PAID` pueden descargar PDF
- El enum `InvoiceStatus` tiene: `DRAFT`, `SENT`, `PAID`, `CANCELLED`

---

## Contract-First con OpenAPI

- Contrato: `contract/contract-billing.yaml` (OpenAPI 3.0.1)
- Plugin: `openapi-generator-maven-plugin 7.3.0`
- Genera:
  - Interfaces API: `ClientsApi`, `InvoicesApi`
  - DTOs: `ClientDTO`, `NewClientDTO`, `InvoiceDTO`, `NewInvoiceDTO`, `InvoiceLine`, `ApiError`
- Los controllers **implementan** las interfaces generadas
- Regenerar: `mvn clean compile`

> ⚠️ Los archivos en `infrastructure/rest/dto/` y las interfaces `*Api` son GENERADOS. **NUNCA editarlos manualmente**.

---

## Checklist para Nueva Entidad

1. [ ] Modelo de dominio en `domain/{entidad}/model/` (Java puro con validación)
2. [ ] Interfaces de use case en `domain/{entidad}/port/in/` (un `execute()` por interfaz)
3. [ ] Commands como records en `domain/{entidad}/port/in/` (validación en compact constructor)
4. [ ] Puerto de salida en `domain/{entidad}/port/out/`
5. [ ] Use case services en `application/useCase/` (`@Service`, constructor explícito)
6. [ ] Entidad JPA en `infrastructure/persistence/entity/` (Lombok + JPA)
7. [ ] Spring Data repo en `infrastructure/persistence/repository/`
8. [ ] Adaptador JPA en `infrastructure/persistence/adapter/` (`@Component`)
9. [ ] Mapper de persistencia en `infrastructure/persistence/mapper/` (`@Component`)
10. [ ] Actualizar contrato OpenAPI `contract/contract-billing.yaml`
11. [ ] `mvn clean compile` para generar interfaces y DTOs
12. [ ] Controller en `infrastructure/rest/api/` (implementa interfaz generada)
13. [ ] Mapper REST en `infrastructure/rest/mapper/` (`@Component`)
14. [ ] Mensajes de error en `ErrorMessages.java` si necesario
15. [ ] Tests: dominio, use case (fakes), controller (MockMvc)
