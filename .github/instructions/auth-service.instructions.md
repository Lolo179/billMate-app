---
applyTo: "auth-service/**"
---

# Especialista: Auth Service – Arquitectura por Capas

> Estas instrucciones se activan automáticamente al editar cualquier archivo dentro de `auth-service/`.

---

## Arquitectura

**Patrón:** Controller → Service (interfaz + impl) → Repository (Spring Data JPA)

```
com.billMate.auth
├── AuthServiceApplication.java
├── config/
│   ├── SecurityConfig.java           # SecurityFilterChain, CORS, BCrypt
│   └── DataSeeder.java               # CommandLineRunner — seed usuario admin
├── controller/
│   └── AuthController.java           # @RestController @RequestMapping("/auth")
├── dto/
│   ├── RegisterRequest.java          # username, email, password, role
│   ├── LoginRequest.java             # email, password
│   └── AuthResponse.java             # token
├── model/
│   ├── User.java                     # @Entity (tabla app_users)
│   ├── Role.java                     # Enum: USER, ADMIN
│   └── UserDTO.java                  # id, username, email, roles
├── repository/
│   └── UserRepository.java           # JpaRepository<User, Long>
└── service/
    ├── AuthService.java              # Interfaz
    ├── JwtService.java               # Generación y validación de tokens
    └── impl/
        └── AuthServiceImpl.java      # Implementación
```

---

## Endpoints

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| POST | `/auth/register` | Público | Registrar nuevo usuario |
| POST | `/auth/login` | Público | Login, retorna JWT |
| GET | `/auth/users` | ADMIN (verificado en servicio) | Listar usuarios |

---

## JWT

- **Librería:** jjwt 0.11.5 (io.jsonwebtoken)
- **Algoritmo:** HS256
- **Expiración:** 24 horas
- **Secreto compartido:** `gJ1yu5QC/hlLhx9d0tqZxIHH8vioFhmv9XCkUdDAX1Y=`
- **Claims:** `sub` = email del usuario, `roles` = lista de roles
- **Configuración:** `jwt.secret` en `application.yaml`

---

## Seguridad

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/register", "/auth/login", "/auth/users").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .anyRequest().authenticated()
                )
                .build();
    }
}
```

- CSRF deshabilitado
- CORS configurado para puertos 8080–8083 + `127.0.0.1:*`
- Métodos permitidos: GET, POST, PUT, DELETE, OPTIONS
- `/auth/**` permitido a todos en Spring Security
- Control de rol ADMIN para `/auth/users` verificado **manualmente** en `AuthServiceImpl`
- Password encoding: `BCryptPasswordEncoder`

---

## Patrones de Código

### Controller

```java
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest registerRequest) {
        return ResponseEntity.ok(authService.register(registerRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return ResponseEntity.ok(authService.getAllUsers(token));
    }
}
```

### Interfaz de Servicio

```java
public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    List<UserDTO> getAllUsers(String token);
}
```

### DTOs

```java
// DTOs en auth usan el patrón Request/Response (no DTO)
public class LoginRequest {    // email, password
public class RegisterRequest { // username, email, password, role
public class AuthResponse {    // token
public class UserDTO {         // id, username, email, roles
```

---

## Base de Datos

- **PostgreSQL 16**, puerto `5434`
- Database: `auth_db`
- `ddl-auto: update` (Hibernate crea/actualiza tablas)
- `sql.init.mode: never` (no ejecuta scripts SQL al arrancar)
- Docker Compose integrado con `spring-boot-docker-compose`

### Tablas

```sql
app_users (id BIGSERIAL PK, email VARCHAR UNIQUE, username VARCHAR UNIQUE, password VARCHAR)
user_roles (user_id BIGINT FK→app_users ON DELETE CASCADE, role VARCHAR)
```

### DataSeeder

`DataSeeder` (`CommandLineRunner`) crea el usuario admin al arrancar si no existe:
- Email: `admin@mail.com`
- Password: `admin123` (BCrypt encoded)
- Rol: `ADMIN`

---

## Roles

```java
public enum Role {
    USER,   // Asignado por defecto al registrarse
    ADMIN   // Solo asignado por DataSeeder o manualmente
}
```

---

## Configuración

```yaml
server:
  port: 8081

jwt:
  secret: gJ1yu5QC/hlLhx9d0tqZxIHH8vioFhmv9XCkUdDAX1Y=

spring:
  datasource:
    url: jdbc:postgresql://localhost:5434/auth_db
    username: postgres
    password: postgres
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

---

## Testing

- Tests de integración con **Testcontainers** (PostgreSQL 16-alpine)
- Clase base: `AuthIntegrationTestBase` con `@ServiceConnection`
- Profile: `@ActiveProfiles("test")`
- Test config: `application-test.yaml` con `ddl-auto: create-drop`, docker compose disabled

```bash
cd auth-service && mvn clean verify
```

---

## Checklist para Nueva Funcionalidad

1. [ ] Crear DTO en `dto/` (record o clase)
2. [ ] Añadir método en `AuthService` (interfaz)
3. [ ] Implementar en `AuthServiceImpl`
4. [ ] Añadir endpoint en `AuthController`
5. [ ] Actualizar `SecurityConfig` si la ruta necesita control de acceso
6. [ ] Test de integración extendiendo `AuthIntegrationTestBase`
