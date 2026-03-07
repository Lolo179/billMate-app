# Auth Service – Arquitectura por Capas

## Arquitectura

**Patrón:** Controller → Service (interfaz + impl) → Repository (Spring Data JPA)

```
com.billMate.auth
├── AuthServiceApplication.java
├── config/
│   ├── SecurityConfig.java       # SecurityFilterChain, CORS, BCrypt
│   └── DataSeeder.java           # CommandLineRunner — seed usuario admin
├── controller/AuthController.java # @RestController @RequestMapping("/auth")
├── dto/
│   ├── RegisterRequest.java      # username, email, password, role
│   ├── LoginRequest.java         # email, password
│   └── AuthResponse.java         # token
├── model/
│   ├── User.java                 # @Entity (tabla app_users)
│   ├── Role.java                 # Enum: USER, ADMIN
│   └── UserDTO.java              # id, username, email, roles
├── repository/UserRepository.java # JpaRepository<User, Long>
└── service/
    ├── AuthService.java          # Interfaz
    ├── JwtService.java           # Generación y validación de tokens
    └── impl/AuthServiceImpl.java # Implementación
```

## Endpoints

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| POST | `/auth/register` | Público | Registrar nuevo usuario |
| POST | `/auth/login` | Público | Login, retorna JWT |
| GET | `/auth/users` | ADMIN | Listar usuarios |

## JWT

- **Librería:** jjwt 0.11.5 (io.jsonwebtoken), HS256, expiración 24h
- **Claims:** `sub` = email del usuario, `roles` = lista de roles
- **Config:** `jwt.secret` en `application.yaml`

## Seguridad

- CSRF deshabilitado, CORS configurado para puertos 8080–8083
- Métodos permitidos: GET, POST, PUT, DELETE, OPTIONS
- `/auth/**` permitido a todos en Spring Security
- Control de rol ADMIN para `/auth/users` verificado **manualmente** en `AuthServiceImpl`
- Password: `BCryptPasswordEncoder`

## DTOs

Auth usa el patrón `Request/Response` (no `DTO`):

- `LoginRequest` — email, password
- `RegisterRequest` — username, email, password, role
- `AuthResponse` — token
- `UserDTO` — id, username, email, roles

## Base de Datos

PostgreSQL 16, puerto **5434**, base de datos `auth_db`. Docker Compose: `auth-service/docker-compose.yaml`.

### Esquema

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
```

### Data Seeding (`DataSeeder.java`)

`CommandLineRunner` que inserta admin si no existe: email `admin@mail.com`, password `admin123` (BCrypt), rol `ADMIN`.

## Testing

Tests de integración con Testcontainers (PostgreSQL 16-alpine).

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

## Observabilidad

### Correlation ID (`CorrelationIdFilter`)

`OncePerRequestFilter` con `@Order(HIGHEST_PRECEDENCE)`. Lee `x-Correlation-Id` del header (propagado por API Gateway) y lo coloca en MDC.

### Logging Estructurado

`logback-spring.xml` con `LogstashEncoder` (JSON). Todos los logs incluyen `correlationId` automáticamente desde MDC.

```java
import static net.logstash.logback.argument.StructuredArguments.kv;
log.info("Registering user", kv("email", request.getEmail()), kv("username", request.getUsername()));
log.warn("Login failed: user not found", kv("email", request.getEmail()));
```

- Heredar de `AuthIntegrationTestBase`
- `@ActiveProfiles("test")` activa `application-test.yaml`
- `@ServiceConnection` auto-configura datasource

### Configuración de Test (`application-test.yaml`)

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

## Reglas de Código

- `@RequiredArgsConstructor` (Lombok) en el controller
- Interfaz `AuthService` + implementación `AuthServiceImpl`
- `@Entity` con Lombok (`@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`)
- Puerto: **8081**
