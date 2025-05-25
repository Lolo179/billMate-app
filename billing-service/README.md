# Billing Service – BillMate

Microservicio encargado de la gestión de clientes y facturación dentro del sistema BillMate.

---

## 📄 Descripción

Este módulo contiene:

- La lógica de negocio relacionada con facturación
- El contrato OpenAPI (`contract-billing.yaml`) ubicado en `src/main/resources/openapi/`
- La generación automática de interfaces e instancias de modelo a partir del contrato

---

## ⚙️ Generación de código desde OpenAPI

Este proyecto sigue una arquitectura **contract-first**. A partir del contrato `contract-billing.yaml`, se generan automáticamente:

- Interfaces de la API (`ClientsApi`, etc.)
- Clases de dominio (`Client`, etc.)

### 📁 Archivos generados

Los archivos generados se encuentran en:
target/generated-sources/openapi/src/main/java/com/billMate/billing/api
target/generated-sources/openapi/src/main/java/com/billMate/billing/model
> ⚠️ Estos archivos **no deben ser modificados manualmente** ni versionados en Git.

---

## 🔧 Comandos útiles

### ▶️ Compilar y generar clases desde el contrato

```bash

mvn clean install
```

Puedes visualizar el contrato OpenAPI directamente en Swagger Editor:

[![Ver en Swagger Editor](https://img.shields.io/badge/Swagger--UI-View%20Contract-green?logo=swagger)](https://editor.swagger.io/?url=https://raw.githubusercontent.com/Lolo179/billMate-app/feature/billing-service/src/main/resources/openapi/contract-billing.yaml)

