---
applyTo: "api-gateway/**"
---

# Especialista: API Gateway – Spring Cloud Gateway (Reactivo)

> Estas instrucciones se activan automáticamente al editar cualquier archivo dentro de `api-gateway/`.

---

## Arquitectura

El API Gateway es **reactivo** (basado en Spring WebFlux / Project Reactor). Actúa como punto de entrada único para todos los microservicios y se encarga de:

1. **Enrutamiento** de peticiones a los microservicios correspondientes
2. **Validación de JWT** mediante filtro reactivo
3. **Control de acceso** basado en roles (ADMIN, USER)

```
com.billMate.gateway
├── GatewayApplication.java              # @SpringBootApplication
├── config/
│   └── SecurityConfig.java              # ServerHttpSecurity (reactivo)
├── filter/
│   └── AuthenticationFilter.java        # WebFilter reactivo
└── util/
    └── JwtUtil.java                     # Validación y extracción de JWT
```

---

## Enrutamiento (application.yaml)

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: auth-service-auth
          uri: http://localhost:8081
          predicates:
            - Path=/auth/**

        - id: billing-service
          uri: http://localhost:8082
          predicates:
            - Path=/billing/**
          filters:
            - StripPrefix=1
```

| Ruta entrante | Servicio destino | Puerto | Transformación |
|---|---|---|---|
| `/auth/**` | Auth Service | 8081 | Sin cambios |
| `/billing/**` | Billing Service | 8082 | `StripPrefix=1` (`/billing/clients` → `/clients`) |

> **Importante:** `StripPrefix=1` elimina el prefijo `/billing` antes de reenviar al billing-service.

---

## Autenticación (AuthenticationFilter)

Filtro reactivo `WebFilter` que intercepta TODAS las peticiones:

### Flujo:

```
Petición → ¿Ruta pública? → [SÍ] → Pasar al servicio
                            → [NO] → ¿Tiene token Bearer? → [NO] → 401 Unauthorized
                                                            → [SÍ] → ¿Token válido? → [NO] → 401 Unauthorized
                                                                                      → [SÍ] → ¿Tiene permiso? → [NO] → 403 Forbidden
                                                                                                                  → [SÍ] → Pasar al servicio
```

### Rutas públicas (sin autenticación):

```java
path.equals("/auth/login")
path.equals("/auth/register")
path.equals("/login")
path.equals("/")
path.startsWith("/actuator/")
path.startsWith("/facturas")
path.equals("/dashboard")
path.equals("/clientes")
path.startsWith("/plugins/")
path.startsWith("/dist/")
path.startsWith("/css/")
path.startsWith("/js/")
path.startsWith("/clientes/")
path.startsWith("/facturas-cliente")
path.equals("/usuarios")
```

### Reglas de acceso:

| Ruta | Requisito |
|---|---|
| `/billing/**` | Rol `USER` o `ADMIN` |
| `/auth/users` | Solo rol `ADMIN` |
| Rutas públicas | Sin autenticación |

### Patrón de código:

```java
@Component
@RequiredArgsConstructor
public class AuthenticationFilter implements WebFilter {

    private final JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        // 1. Rutas públicas → pasar directo
        if (isPublicRoute(path)) {
            return chain.filter(exchange);
        }

        // 2. Extraer y validar token
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.isTokenValid(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // 3. Extraer roles y verificar acceso
        String email = jwtUtil.extractEmail(token);
        List<String> roles = jwtUtil.extractRoles(token);

        // 4. Crear contexto de seguridad reactivo
        List<GrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());

        var auth = new UsernamePasswordAuthenticationToken(email, null, authorities);
        var context = new SecurityContextImpl(auth);

        return chain.filter(exchange)
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(context)));
    }
}
```

---

## JwtUtil

```java
public class JwtUtil {
    // Métodos principales:
    boolean isTokenValid(String token);   // Verifica firma y expiración
    String extractEmail(String token);     // Extrae subject (email)
    List<String> extractRoles(String token); // Extrae claim "roles"
}
```

- Secreto JWT: `gJ1yu5QC/hlLhx9d0tqZxIHH8vioFhmv9XCkUdDAX1Y=` (compartido con auth-service)
- Algoritmo: HS256
- Librería: jjwt 0.11.5

---

## SecurityConfig (Reactivo)

```java
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        // Configuración de seguridad reactiva (WebFlux)
    }
}
```

> **Nota:** Este servicio usa `ServerHttpSecurity` (WebFlux), NO `HttpSecurity` (Servlet). Nunca mezclar APIs servlet con reactivas.

---

## Dependencias Clave

- `spring-cloud-starter-gateway` (reactivo, basado en WebFlux)
- `spring-boot-starter-security` (seguridad reactiva)
- `jjwt-api`, `jjwt-impl`, `jjwt-jackson` (0.11.5)
- `lombok`
- `spring-boot-starter-actuator`

---

## Configuración

```yaml
server:
  port: 8080

jwt:
  secret: gJ1yu5QC/hlLhx9d0tqZxIHH8vioFhmv9XCkUdDAX1Y=

management:
  endpoints:
    web:
      exposure:
        include: health
  endpoint:
    health:
      show-details: always
```

---

## Reglas Importantes

1. **Todo es reactivo**: usar `Mono`, `Flux`, `WebFilter`, `ServerWebExchange` — NUNCA APIs de Servlet
2. **No bloquear hilos**: nunca usar `.block()` en producción
3. **El gateway NO tiene base de datos** — solo valida tokens
4. **Prefijo `/billing/`**: se elimina antes de reenviar (StripPrefix=1)
5. **Los logs de debug usan emojis** para trazabilidad (🌐, 🔓, ⛔, 🔐, ✅, 🚫, 🎭)

---

## Checklist para Nuevas Rutas

1. [ ] Añadir ruta en `application.yaml` (sección `spring.cloud.gateway.routes`)
2. [ ] Si es ruta pública, añadirla en `AuthenticationFilter` (lista de rutas públicas)
3. [ ] Si necesita control de rol, añadir verificación en `AuthenticationFilter`
4. [ ] Actualizar `SecurityConfig` si hay nuevos patrones de autorización
