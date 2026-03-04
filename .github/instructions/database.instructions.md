---
applyTo: "**/*.sql,**/entity/**,**/persistence/**"
---

# Especialista: Base de Datos – PostgreSQL + JPA + Persistencia

> Estas instrucciones se activan automáticamente al editar archivos SQL, entidades JPA o adaptadores de persistencia.

---

## Bases de Datos

| Base de datos | Servicio | Puerto | Usuario | Password |
|---|---|---|---|---|
| `auth_db` | Auth Service | 5434 | postgres | postgres |
| `billing_db` | Billing Service | 5433 | postgres | postgres |

Ambas PostgreSQL 16, ejecutadas vía Docker Compose.

---

## Esquema Auth DB

```sql
CREATE TABLE IF NOT EXISTS app_users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role VARCHAR(255) NOT NULL,
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id)
        REFERENCES app_users(id) ON DELETE CASCADE
);

-- Índices
CREATE INDEX IF NOT EXISTS idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX IF NOT EXISTS idx_app_users_email ON app_users(email);
CREATE INDEX IF NOT EXISTS idx_app_users_username ON app_users(username);
```

---

## Esquema Billing DB

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

-- Índices
CREATE INDEX IF NOT EXISTS idx_clients_email ON clients(email);
CREATE INDEX IF NOT EXISTS idx_invoices_client_id ON invoices(client_id);
CREATE INDEX IF NOT EXISTS idx_invoices_status ON invoices(status);
CREATE INDEX IF NOT EXISTS idx_invoices_date ON invoices(date);
CREATE INDEX IF NOT EXISTS idx_invoice_lines_invoice_id ON invoice_lines(invoice_id);
```

---

## Convenciones SQL

| Concepto | Convención | Ejemplo |
|---|---|---|
| Nombres de tabla | snake_case, plural | `clients`, `invoices`, `invoice_lines` |
| Primary keys | `{tabla_singular}_id` o `id` | `client_id`, `invoice_id`, `id` |
| Foreign keys | `{tabla_referenciada_singular}_id` | `client_id`, `invoice_id` |
| Tipos numéricos monetarios | `NUMERIC(10, 2)` | `total`, `unit_price` |
| Porcentajes | `NUMERIC(5, 2)` | `tax_percentage` |
| Timestamps | `TIMESTAMP WITH TIME ZONE` o `TIMESTAMP` | `created_at` |
| Fechas | `DATE` | `date` |
| Strings | `VARCHAR(255)` por defecto, `VARCHAR(100)` para cortos, `VARCHAR(50)` para enums | — |
| Booleanos | — (no usados actualmente) | — |
| Sequences | `BIGSERIAL` (auto-generado por PostgreSQL) | — |
| FK cascada | `ON DELETE CASCADE` | Siempre |
| Constraint naming | `fk_{tabla}_{referencia}` | `fk_invoices_client` |
| Índices | `idx_{tabla}_{columna}` | `idx_invoices_client_id` |

---

## Entidades JPA (Billing Service)

### Patrón de Entidad

```java
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

**Reglas para entidades JPA:**
- Lombok: `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`
- `@Table(name = "nombre_tabla")` explícito
- `@Column(name = "...")` cuando el nombre de columna difiere del campo Java
- `@GeneratedValue(strategy = GenerationType.IDENTITY)` para PKs auto-generadas
- `@Id` siempre de tipo `Long`
- Timestamps como `OffsetDateTime` (mapea a `TIMESTAMP WITH TIME ZONE`)

### Relaciones

```java
// InvoiceEntity → ClientEntity (ManyToOne)
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "client_id", nullable = false)
private ClientEntity client;

// InvoiceEntity → InvoiceLineEntity (OneToMany)
@OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
private List<InvoiceLineEntity> lines = new ArrayList<>();
```

- `FetchType.LAZY` por defecto en `@ManyToOne`
- `CascadeType.ALL` + `orphanRemoval = true` en `@OneToMany`
- `open-in-view: false` en configuración (evitar N+1 accidentales)

---

## Mapper de Persistencia

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
- `toEntity()` usa builder de Lombok
- `toDomain()` usa constructor del modelo de dominio
- Nunca exponer entidades JPA fuera de la capa de infraestructura

---

## Adaptador JPA

```java
@Component
public class ClientJpaAdapter implements ClientRepositoryPort {

    private final SpringDataClientRepository springDataClientRepository;
    private final ClientPersistenceMapper clientPersistenceMapper;

    // Constructor explícito

    @Override
    public Client save(Client client) {
        ClientEntity entity = clientPersistenceMapper.toEntity(client);
        ClientEntity saved = springDataClientRepository.save(entity);
        return clientPersistenceMapper.toDomain(saved);
    }
    // ... findById, findAll, deleteById, existsById
}
```

**Reglas:**
- `@Component` (no `@Repository`, eso es para Spring Data)
- Implementa el puerto de salida del dominio
- Siempre mapea: dominio → entidad → save → entidad → dominio

---

## Configuración Hibernate

### Producción:
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update            # Auto-crea/actualiza tablas
    open-in-view: false           # Evitar lazy loading accidental
    show-sql: true
    properties:
      hibernate:
        format_sql: true
```

### Test:
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: create            # Billing (crear desde cero)
      ddl-auto: create-drop       # Auth (crear y destruir)
  sql:
    init:
      mode: never                 # No ejecutar data.sql en tests
```

---

## Data Seeding

### Billing Service (`data.sql`):
- Se ejecuta en **cada arranque** (`sql.init.mode: always`)
- Hace `TRUNCATE` de todas las tablas + reset de secuencias
- Inserta ~25 clientes, ~35 facturas y líneas de factura
- Solo en desarrollo/local — **nunca en tests** (`mode: never`)

### Auth Service (`DataSeeder.java`):
- `CommandLineRunner` que inserta admin si no existe
- Email: `admin@mail.com`, Password: `admin123` (BCrypt), Rol: `ADMIN`

---

## Script de Inicialización

```bash
psql -U postgres -f scripts/create-tables.sql
```

Este script crea ambas bases de datos y todas las tablas con índices.

---

## Docker Compose para Desarrollo

```bash
# Auth DB (puerto 5434)
docker-compose -f auth-service/docker-compose.yaml up -d

# Billing DB (puerto 5433)
docker-compose -f billing-service/docker-compose.yaml up -d
```

Spring Boot gestiona el ciclo de vida automáticamente con `spring-boot-docker-compose`.
