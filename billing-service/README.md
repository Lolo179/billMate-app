# Billing Service – BillMate

Microservicio encargado de la gestión de clientes, facturas y productos dentro del sistema BillMate, implementado con arquitectura **contract-first** usando OpenAPI.

---

## 📄 Descripción

Este módulo contiene:

- La lógica de negocio relacionada con facturación
- El contrato OpenAPI (`contract-billing.yaml`) ubicado en `contract/`
- Generación automática de interfaces e instancias de modelo a partir del contrato
- Endpoints REST para gestión de clientes y facturas

---

## 🔧 Arquitectura Contract-First

Este proyecto sigue una arquitectura **contract-first**. A partir del contrato `contract-billing.yaml`, se generan automáticamente:

- Interfaces de la API (`ClientsApi`, `InvoicesApi`, etc.)
- Clases de dominio (`Client`, `Invoice`, `InvoiceLine`, etc.)

### 📁 Archivos Generados

Los archivos generados se encuentran en:

```
target/generated-sources/openapi/src/main/java/com/billMate/billing/api
target/generated-sources/openapi/src/main/java/com/billMate/billing/model
```

> ⚠️ Estos archivos **no deben ser modificados manualmente** ni versionados en Git.

---

## 🛠️ Stack Tecnológico

- Java 21 (LTS)
- Spring Boot 3.3.0
- Spring Data JPA
- PostgreSQL
- OpenAPI / Swagger
- Maven

---

## 🔧 Configuración por Defecto

El servicio se levanta en el puerto:

```
http://localhost:8082
```

Y utiliza la base de datos PostgreSQL `billmate_billing`. Puedes ajustar estos valores desde el archivo:

```
src/main/resources/application.yaml
```

---

## 🚀 Compilar y Generar Clases desde el Contrato

Para generar las clases desde el contrato OpenAPI:

```bash
cd billing-service
mvn clean install
```

Esto ejecutará el plugin `openapi-generator-maven-plugin` y generará automáticamente las clases.

---

## 📊 CI/CD

Este servicio dispone de un workflow automático en GitHub Actions:

- **Archivo**: `.github/workflows/billing-ci.yaml`
- **Trigger**: 
  - PR a rama `develop`
  - Push a rama `main`
- **Acciones**:
  - ✅ Ejecución de tests (`mvn clean verify`)
  - ✅ Build con Java 21 y cache Maven
  - ✅ Construcción de imagen Docker en push a `main`

---

## 📋 Contrato OpenAPI

El contrato de la API se encuentra en:

```
contract/contract-billing.yaml
```

Puedes visualizarlo directamente en Swagger Editor:

[![Ver en Swagger Editor](https://img.shields.io/badge/Swagger--UI-View%20Contract-green?logo=swagger)](https://editor.swagger.io/?url=https://raw.githubusercontent.com/Lolo179/billMate-app/main/billing-service/contract/contract-billing.yaml)

O acceder a Swagger UI cuando el servicio esté corriendo:

```
http://localhost:8082/swagger-ui.html
```

---

## 🧪 Testing

Para ejecutar los tests del servicio:

```bash
cd billing-service
mvn clean verify
```

Los tests están ubicados en:

```
src/test/java/com/billMate/billing/
```

---

## 🐳 Docker

Para construir la imagen Docker:

```bash
docker build -t billmate/billing-service:latest .
```

Para ejecutar con docker-compose:

```bash
docker-compose up -d
```

---

## 🔐 Seguridad

- Los endpoints de este servicio están protegidos por JWT
- El token debe ser validado a través del **API Gateway**
- Las peticiones deben incluir el header:

```
Authorization: Bearer <tu-token-jwt>
```

---

## 📚 Referencias

- [BillMate Principal README](../README.md)
- [Auth Service](../auth-service/README.md)
- [API Gateway](../api-gateway/README.md)
- [Database Setup](../scripts/README-DATABASE.md)

