# API Gateway – Spring Cloud Gateway (Reactivo)

## Arquitectura

Punto de entrada único para todos los microservicios. **100% reactivo** (Spring WebFlux / Project Reactor).

```
com.billMate.gateway
├── GatewayApplication.java          # @SpringBootApplication
├── config/SecurityConfig.java       # ServerHttpSecurity (reactivo)
├── filter/AuthenticationFilter.java # WebFilter reactivo (validación JWT)
└── util/JwtUtil.java                # Validación y extracción de JWT
```

**Responsabilidades:** enrutamiento, validación JWT reactiva, control de acceso por roles (ADMIN, USER).

## Enrutamiento

| Ruta entrante | Servicio destino | Puerto | Transformación |
|---|---|---|---|
| `/auth/**` | Auth Service | 8081 | Sin cambios |
| `/billing/**` | Billing Service | 8082 | `StripPrefix=1` (`/billing/clients` → `/clients`) |

> `StripPrefix=1` elimina el prefijo `/billing` antes de reenviar al billing-service.

## Autenticación (AuthenticationFilter)

Filtro `WebFilter` reactivo que intercepta TODAS las peticiones:

1. ¿Ruta pública? → Pasar
2. ¿Token Bearer presente? → Si no, 401
3. ¿Token válido? → Si no, 401
4. ¿Tiene permiso? → Si no, 403 → Si sí, pasar

### Rutas públicas (sin autenticación)

`/auth/login`, `/auth/register`, `/login`, `/`, `/actuator/**`, `/dashboard`, `/clientes`, `/clientes/**`, `/facturas`, `/facturas/**`, `/facturas-cliente`, `/usuarios`, `/plugins/**`, `/dist/**`, `/css/**`, `/js/**`

### Reglas de acceso

| Ruta | Requisito |
|---|---|
| `/billing/**` | Rol `USER` o `ADMIN` |
| `/auth/users` | Solo rol `ADMIN` |

## Reglas de Código

- **Reactivo siempre**: usar `Mono`, `Flux`, `ServerWebExchange` — nunca APIs bloqueantes
- `@RequiredArgsConstructor` (Lombok) permitido en filtros y config
- `ServerHttpSecurity` (no `HttpSecurity`) para configuración de seguridad

## Observabilidad

### Correlation ID (`CorrelationIdFilter`)

`GlobalFilter + Ordered` con `HIGHEST_PRECEDENCE`. Genera UUID en `x-Correlation-Id` si no existe, lo propaga en:
- Request header (downstream hacia auth/billing)
- Response header (upstream hacia el cliente)
- MDC con clave `correlationId` (para logs)

### Logging Estructurado

`logback-spring.xml` con `LogstashEncoder` (JSON). Todos los logs incluyen `correlationId` automáticamente desde MDC.

```java
import static net.logstash.logback.argument.StructuredArguments.kv;
log.debug(">> Incoming request", kv("method", method), kv("path", path));
```
- No hay controllers — toda la lógica es enrutamiento + filtros
- Puerto: **8080**
