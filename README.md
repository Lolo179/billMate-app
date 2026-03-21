# BillMate

**BillMate** es una aplicación de facturación para pequeños negocios, construida con arquitectura de microservicios, desarrollada como proyecto de TFG en el ciclo de DAW.

---

## 🏗️ Arquitectura General

- **Auth Service** (Puerto 8081): Registro y autenticación de usuarios con JWT
- **Billing Service** (Puerto 8082): Gestión de facturas, clientes y productos – **Arquitectura Hexagonal (Ports & Adapters)** + contract-first con OpenAPI
- **Notification Service** (Puerto 8084): Servicio ficticio de notificaciones por email – consume eventos Kafka de billing-service
- **API Gateway** (Puerto 8080): Entrada central para peticiones, validación de JWT
- **Frontend Service** (Puerto 5173 en desarrollo, 8083 en contenedor): React + TypeScript + Vite

### Arquitectura Hexagonal en Billing Service

El servicio de facturación sigue estrictamente el patrón hexagonal:

- **Dominio** puro sin dependencias a frameworks (modelos, puertos de entrada/salida, commands)
- **Aplicación** con casos de uso que solo dependen de puertos de dominio
- **Infraestructura** con adaptadores REST, JPA y PDF, y mappers dedicados por capa
- Sin servicios legacy – toda la lógica fluye a través de use cases

---

## 🛠️ Tecnologías Utilizadas

- **Java 21 (LTS)**, **Spring Boot 3.3.0**
- **Spring Security + JWT**
- **Spring Cloud Gateway (reactivo)**
- **Spring Data JPA + PostgreSQL**
- **Maven** para gestión de dependencias
- **Apache Kafka** para eventos entre microservicios
- **Docker** & **Docker Compose** para containerización
- **GitHub Actions** para CI/CD
- **Swagger/OpenAPI** para documentación
- **Git + GitHub** para control de versiones

---

## 🚀 Inicio Rápido

### Requisitos Previos

- JDK 21 instalado
- PostgreSQL 12+
- Maven 3.8+
- Docker y Docker Compose (opcional, para ejecución containerizada)

### Opción 1: Ejecución Local con Maven

#### 1. Preparar Base de Datos

```bash
psql -U postgres -f scripts/create-tables.sql
```

#### 2. Iniciar Servicios

Desde el directorio raíz del proyecto, ejecuta los scripts de inicio en orden:

```bash
# Windows
.\scripts\auth-service.bat
.\scripts\api-gateway.bat
.\scripts\billing-service.bat
.\scripts\frontend-service.bat

# Linux/Mac
bash scripts/auth-service.bat
bash scripts/api-gateway.bat
bash scripts/billing-service.bat
bash scripts/frontend-service.bat
```

O instala todo de una vez:

```bash
# Windows
.\scripts\install-all.bat

# Linux/Mac
bash scripts/install-all.bat
```

**Servicios disponibles después de iniciar:**

- **Auth Service**: http://localhost:8081
- **API Gateway**: http://localhost:8080
- **Billing Service**: http://localhost:8082
- **Frontend**: http://localhost:5173
- **Notification Service**: http://localhost:8084
- **Grafana (Observabilidad)**: http://localhost:3000 (admin/admin)
- **Kafka UI**: http://localhost:9090

### Opción 2: Ejecución con Docker Compose

```bash
docker-compose -f auth-service/docker-compose.yaml up -d
docker-compose -f billing-service/docker-compose.yaml up -d
docker-compose -f kafka/docker-compose.yaml up -d
docker-compose -f observability/docker-compose.yaml up -d
```

---

## 🔐 Autenticación y Seguridad

- El sistema genera tokens JWT al login
- Las rutas del `billing-service` están protegidas por el `api-gateway`
- Se valida JWT directamente en el gateway con un filtro reactivo personalizado
- Incluye el token en el header Authorization en todas las peticiones protegidas:

```
Authorization: Bearer <tu-token-jwt>
```

---

## 📊 CI/CD con GitHub Actions

### CI — Por servicio (PRs a `main`)

| Workflow | Qué valida |
|---|---|
| `auth-ci.yaml` | Tests + build Docker de auth-service |
| `billing-ci.yaml` | Tests + build Docker de billing-service |
| `api-gateway-ci.yaml` | Tests + build Docker del api-gateway |
| `frontend-ci.yaml` | Tests unitarios + build Vite + build Docker |
| `e2e-ci.yaml` | Entorno completo + pruebas E2E Playwright |

Todos se ejecutan en PR a `main` con concurrencia `cancel-in-progress: true`.

### CD — Pipeline global (`deploy.yaml`)

Activado en **push a `main`** o manualmente. Tres jobs en este orden:

```
  build-and-push ─┐
                  ├─► deploy (EC2)
        e2e ──────┘
```

- **`build-and-push`**: construye y publica las 4 imágenes en GHCR (`:latest`) en paralelo con los E2E
- **`e2e`**: levanta el entorno completo (JVM + Docker) y ejecuta Playwright
- **`deploy`**: SSH a EC2 → `docker compose pull && up -d` → health check

EC2 solo se actualiza si **ambos** jobs paralelos pasan.

### E2E — Estrategia híbrida (JVM + Docker)

- `auth-db`, `billing-db` y `kafka` corren como contenedores Docker
- `auth-service`, `billing-service` y `api-gateway` arrancan como procesos JVM en el runner
- `frontend-service` arranca con Vite dev server (`VITE_USE_MSW=false`)

---

## 📁 Estructura del Proyecto

```
billMate-app/
├── auth-service/           # Microservicio de autenticación (JWT)
├── api-gateway/            # API Gateway (enrutamiento + validación JWT)
├── billing-service/        # Microservicio de facturación (Arquitectura Hexagonal)
│   ├── domain/             #   Modelos, puertos in/out, commands, eventos
│   ├── application/        #   Use cases (sin deps a infraestructura)
│   └── infrastructure/     #   Adapters REST, JPA, PDF, Kafka + mappers dedicados
├── frontend-service/       # Aplicación frontend (React + TypeScript + Vite)
├── notification-service/   # Notificaciones ficticio (consume Kafka events)
├── e2e/                    # Tests E2E Playwright (entorno completo)
│   ├── tests/              #   Escenarios Playwright
│   ├── docker-compose.ci.yaml  #  Infra CI (DBs + gateway + frontend)
│   └── playwright.config.ts
├── observability/          # Docker Compose para Grafana + Loki + Promtail
├── kafka/                  # Docker Compose para Kafka broker + Kafka UI
├── scripts/                # Scripts de instalación e inicialización
├── .github/workflows/      # Configuración CI/CD
│   ├── auth-ci.yaml        #   CI: tests auth-service (PRs)
│   ├── billing-ci.yaml     #   CI: tests billing-service (PRs)
│   ├── api-gateway-ci.yaml #   CI: tests api-gateway (PRs)
│   ├── frontend-ci.yaml    #   CI: tests + build frontend (PRs)
│   ├── e2e-ci.yaml         #   CI: E2E Playwright entorno completo (PRs)
│   └── deploy.yaml         #   CD: build-push + E2E + deploy EC2 (main)
└── README.md               # Este archivo
```

---

## 📝 Endpoints Principales

### Auth Service

| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/auth/register` | Registrar nuevo usuario |
| POST | `/auth/login` | Autenticar y obtener JWT |

### Billing Service (a través de API Gateway)

Consulta `billing-service/README.md` y `billing-service/contract/contract-billing.yaml` para detalles completos.

---

## 🧪 Testing

```bash
# Ejecutar tests en todos los servicios
.\scripts\install-all.bat

# Ejecutar tests en un servicio específico
cd auth-service
mvn clean verify
```

---

## 📚 Documentación Adicional

- [Auth Service README](auth-service/README.md)
- [Billing Service README](billing-service/README.md)
- [Database Setup](scripts/README-DATABASE.md)
- Swagger/OpenAPI disponible en cada servicio (cuando esté corriendo)

---

## 📄 Licencia

Proyecto de TFG - ciclo de DAW
