# BillMate

**BillMate** es una aplicación de facturación para pequeños negocios, construida con arquitectura de microservicios, desarrollada como proyecto de TFG en el ciclo de DAW.

---

## 🏗️ Arquitectura General

- **Auth Service** (Puerto 8081): Registro y autenticación de usuarios con JWT
- **Billing Service** (Puerto 8082): Gestión de facturas, clientes y productos
- **API Gateway** (Puerto 8080): Entrada central para peticiones, validación de JWT
- **Frontend Service** (Puerto 3000): Angular + Bootstrap

---

## 🛠️ Tecnologías Utilizadas

- **Java 21 (LTS)**, **Spring Boot 3.3.0**
- **Spring Security + JWT**
- **Spring Cloud Gateway (reactivo)**
- **Spring Data JPA + PostgreSQL**
- **Maven** para gestión de dependencias
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
- **Frontend**: http://localhost:3000

### Opción 2: Ejecución con Docker Compose

```bash
docker-compose -f auth-service/docker-compose.yaml up -d
docker-compose -f billing-service/docker-compose.yaml up -d
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

Se han configurado workflows automáticos para cada microservicio:

- **`auth-ci.yaml`**: Ejecuta tests y build de auth-service en PR a develop y push a main
- **`billing-ci.yaml`**: Ejecuta tests y build de billing-service en PR a develop y push a main  
- **`api-gateway-ci.yaml`**: Ejecuta tests y build de api-gateway en PR a develop y push a main

**Estos workflows:**
- ✅ Ejecutan `mvn clean verify` con Java 21 y cache Maven
- ✅ Construyen imagen Docker en push a `main` (sin push a registro)
- ✅ Usan concurrencia para cancelar runs anteriores en la misma rama

---

## 📁 Estructura del Proyecto

```
billMate-app/
├── auth-service/           # Microservicio de autenticación
├── api-gateway/            # API Gateway (enrutamiento)
├── billing-service/        # Microservicio de facturación
├── frontend-service/       # Aplicación frontend
├── scripts/                # Scripts de instalación e inicialización
├── .github/workflows/      # Configuración CI/CD
│   ├── auth-ci.yaml
│   ├── billing-ci.yaml
│   └── api-gateway-ci.yaml
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
