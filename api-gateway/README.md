# API Gateway – BillMate

Punto de entrada único para todos los microservicios de BillMate. Implementado con **Spring Cloud Gateway** sobre **Spring WebFlux** (100% reactivo).

---

## 🔧 Descripción

El gateway se encarga de:

- Enrutar peticiones hacia los microservicios internos
- Validar tokens JWT en cada petición protegida
- Controlar el acceso por roles (`ADMIN`, `USER`)
- Generar y propagar el `Correlation ID` para trazabilidad de logs

---

## 🛠️ Stack Tecnológico

- Java 21 (LTS)
- Spring Boot 3.3.0
- Spring Cloud Gateway 2023.0.3 (WebFlux / Project Reactor)
- Spring Security + JWT (jjwt 0.11.5, HS256)
- Maven
- logstash-logback-encoder 7.4 (logs JSON estructurados)

---

## 🔧 Configuración por Defecto

El gateway se levanta en el puerto:

```
http://localhost:8080
```

Puedes ajustar la configuración desde:

```
src/main/resources/application.yaml
```

---

## 🔀 Enrutamiento

| Ruta entrante | Destino | Transformación |
|---|---|---|
| `/auth/**` | Auth Service (8081) | Sin cambios |
| `/billing/**` | Billing Service (8082) | `StripPrefix=1` → `/billing/clients` se convierte en `/clients` |

---

## 🔐 Autenticación (AuthenticationFilter)

Filtro `WebFilter` reactivo que intercepta **todas** las peticiones:

1. ¿Ruta pública? → Pasar sin validación
2. ¿Token Bearer presente? → Si no, `401 Unauthorized`
3. ¿Token válido? → Si no, `401 Unauthorized`
4. ¿Tiene rol suficiente? → Si no, `403 Forbidden`

### Rutas públicas (sin autenticación requerida)

`/auth/login`, `/auth/register`, `/login`, `/`, `/dashboard`, `/clientes/**`, `/facturas/**`, `/facturas-cliente`, `/usuarios`, `/actuator/**`, `/plugins/**`, `/dist/**`, `/css/**`, `/js/**`

### Reglas de acceso

| Ruta | Requisito |
|---|---|
| `/billing/**` | Rol `USER` o `ADMIN` |
| `/auth/users` | Solo rol `ADMIN` |

---

## 📊 CI/CD

### CI — `.github/workflows/api-gateway-ci.yaml`

- **Trigger**: PR a `main`
- **Acciones**:
  - ✅ Build y tests (`mvn clean verify`) con Java 21
  - ✅ Build de imagen Docker (sin push)

### CD — `.github/workflows/deploy.yaml` (pipeline global)

Activado en push a `main` si CI pasa:
- Construye y publica la imagen `ghcr.io/{owner}/api-gateway:latest` en GHCR
- Despliega a EC2 junto al resto de servicios

---

## 🧪 Testing

```bash
cd api-gateway
mvn clean verify
```

---

## 📈 Observabilidad

### Correlation ID (`CorrelationIdFilter`)

`GlobalFilter` con `Ordered.HIGHEST_PRECEDENCE`. Genera un UUID en el header `x-Correlation-Id` si no existe y lo propaga:

- En el **request** hacia los servicios downstream (auth-service, billing-service)
- En el **response** hacia el cliente
- En el **MDC** para que aparezca en todos los logs de esta petición

### Logs JSON estructurados

`logback-spring.xml` con `LogstashEncoder`. Compatible con el stack Grafana + Loki + Promtail:

```bash
docker-compose -f ../observability/docker-compose.yaml up -d   # Grafana en http://localhost:3000
```

---

## 🐳 Docker

```bash
docker build -t billmate/api-gateway:latest .
```

---

## 📚 Referencias

- [BillMate Principal README](../README.md)
- [Auth Service](../auth-service/README.md)
- [Billing Service](../billing-service/README.md)
