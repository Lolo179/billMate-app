# Billing Service – BillMate

Microservicio encargado de la gestión de clientes, facturas y productos dentro del sistema BillMate, implementado con **arquitectura hexagonal (Ports & Adapters)** y enfoque **contract-first** usando OpenAPI.

---

## 📄 Descripción

Este módulo contiene:

- La lógica de negocio de facturación, aislada en el dominio
- Puertos de entrada (use cases) y salida (repository ports) bien definidos
- Mappers dedicados por capa (REST y persistencia)
- El contrato OpenAPI (`contract-billing.yaml`) ubicado en `contract/`
- Generación automática de interfaces e instancias de modelo a partir del contrato
- Endpoints REST para gestión de clientes y facturas

---

## 🏛️ Arquitectura Hexagonal

El servicio sigue estrictamente la arquitectura hexagonal (Ports & Adapters), con una clara separación en tres capas:

### Dominio (`domain/`)

El núcleo de la aplicación, sin dependencias a frameworks ni infraestructura:

- **Modelos**: `Client`, `Invoice`, `InvoiceLineItem`, `InvoiceStatus`
- **Puertos de entrada (in)**: interfaces de casos de uso que definen las operaciones del sistema
  - `CreateClientUseCase`, `GetClientUseCase`, `GetAllClientsUseCase`, `UpdateClientUseCase`, `DeleteClientUseCase`
  - `CreateInvoiceUseCase`, `GetInvoiceUseCase`, `GetAllInvoicesUseCase`, `GetInvoicesByClientUseCase`, `UpdateInvoiceUseCase`, `DeleteInvoiceUseCase`, `EmitInvoiceUseCase`, `DownloadInvoicePdfUseCase`, `PayInvoiceUseCase`
- **Commands**: `CreateClientCommand`, `UpdateClientCommand`, `CreateInvoiceCommand`, `UpdateInvoiceCommand`
- **Puertos de salida (out)**: `ClientRepositoryPort`, `InvoiceRepositoryPort`, `PdfGeneratorPort`, `InvoiceEventPublisherPort`

### Aplicación (`application/useCase/`)

Implementaciones de los casos de uso, que orquestan la lógica de dominio:

- `CreateClientService`, `GetClientService`, `GetAllClientsService`, `UpdateClientService`, `DeleteClientService`
- `CreateInvoiceService`, `GetInvoiceService`, `GetAllInvoicesService`, `GetInvoicesByClientService`, `UpdateInvoiceService`, `DeleteInvoiceService`, `EmitInvoiceService`, `DownloadInvoicePdfService`, `PayInvoiceService`

> Solo dependen de puertos de dominio, nunca de infraestructura.

### Infraestructura (`infrastructure/`)

Adaptadores que conectan el dominio con el mundo exterior:

- **REST** (`rest/api/`): `ClientController`, `InvoiceController` — implementan las interfaces OpenAPI generadas
- **Mappers REST** (`rest/mapper/`): `ClientRestMapper`, `InvoiceRestMapper` — convierten entre modelos de dominio y DTOs
- **Persistencia** (`persistence/adapter/`): `ClientJpaAdapter`, `InvoiceJpaAdapter` — implementan los puertos de salida
- **Mappers Persistencia** (`persistence/mapper/`): `ClientPersistenceMapper`, `InvoicePersistenceMapper` — convierten entre modelos de dominio y entidades JPA
- **PDF** (`pdf/`): `PdfGeneratorAdapter` — implementa `PdfGeneratorPort`
- **Kafka** (`kafka/adapter/`): `InvoiceKafkaAdapter` (`@Async`) — implementa `InvoiceEventPublisherPort`

### Diagrama de Dependencias

```
                    ┌──────────────────────────┐
                    │     REST Controllers      │
                    │  (ClientsApi, InvoicesApi) │
                    └────────────┬─────────────┘
                                 │ usa
                    ┌────────────▼─────────────┐
                    │     REST Mappers          │
                    │ (ClientRestMapper,        │
                    │  InvoiceRestMapper)        │
                    └────────────┬─────────────┘
                                 │ convierte a/desde
          ┌──────────────────────▼──────────────────────┐
          │              DOMINIO (núcleo)                │
          │  Modelos: Client, Invoice, InvoiceLineItem  │
          │  Puertos IN: *UseCase interfaces            │
          │  Puertos OUT: *RepositoryPort, PdfPort,     │
          │              InvoiceEventPublisherPort       │
          │  Commands: Create/Update*Command            │
          └──────────┬──────────────────┬───────────────┘
                     │                  │
        ┌────────────▼──────┐  ┌────────▼──────────────┐
        │  UseCase Services │  │ Persistence Adapters   │
        │  (application/)   │  │ + Persistence Mappers  │
        └───────────────────┘  └───────────────────────┘
```

---

## 🔧 Contract-First con OpenAPI

A partir del contrato `contract-billing.yaml`, se generan automáticamente:

- Interfaces de la API (`ClientsApi`, `InvoicesApi`)
- DTOs (`ClientDTO`, `InvoiceDTO`, `NewClientDTO`, `NewInvoiceDTO`, `InvoiceLine`)

> ⚠️ Estos archivos **no deben ser modificados manualmente** ni versionados en Git.

---

## 📁 Estructura del Proyecto

```
billing-service/
├── contract/
│   └── contract-billing.yaml          # Contrato OpenAPI
├── src/main/java/com/billMate/billing/
│   ├── domain/
│   │   ├── client/
│   │   │   ├── model/Client.java
│   │   │   └── port/
│   │   │       ├── in/                # Use case interfaces + Commands
│   │   │       └── out/               # ClientRepositoryPort
│   │   └── invoice/
│   │       ├── model/                 # Invoice, InvoiceLineItem, InvoiceStatus
│   │       ├── event/                 # InvoiceCreatedEvent (record)
│   │       └── port/
│   │           ├── in/                # Use case interfaces + Commands
│   │           └── out/               # InvoiceRepositoryPort, PdfGeneratorPort, InvoiceEventPublisherPort
│   ├── application/
│   │   └── useCase/                   # Implementaciones de use cases
│   └── infrastructure/
│       ├── config/                    # JpaConfiguration
│       ├── kafka/adapter/             # InvoiceKafkaAdapter (@Async)
│       ├── pdf/                       # PdfGeneratorAdapter
│       ├── persistence/
│       │   ├── adapter/               # ClientJpaAdapter, InvoiceJpaAdapter
│       │   ├── entity/                # ClientEntity, InvoiceEntity, InvoiceLineEntity
│       │   ├── mapper/                # ClientPersistenceMapper, InvoicePersistenceMapper
│       │   └── repository/            # SpringDataClientRepository, SpringDataInvoiceRepository
│       └── rest/
│           ├── api/                   # ClientController, InvoiceController
│           ├── dto/                   # DTOs generados por OpenAPI
│           ├── error/                 # GlobalExceptionHandler, ErrorMessages
│           └── mapper/                # ClientRestMapper, InvoiceRestMapper
└── src/test/java/com/billMate/billing/
    ├── application/useCase/           # Tests unitarios de use cases
    ├── domain/client/model/           # Tests de validación de dominio
    └── infrastructure/rest/api/       # Tests de controllers
```

---

## 🛠️ Stack Tecnológico

- Java 21 (LTS)
- Spring Boot 3.3.0
- Spring Data JPA
- PostgreSQL
- Apache Kafka 3.8.0 (KRaft) + Spring Kafka
- OpenAPI / Swagger
- iText (generación de PDF)
- Maven
- Lombok

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

Los tests están organizados por capa siguiendo la arquitectura hexagonal:

```
src/test/java/com/billMate/billing/
├── application/useCase/           # Tests unitarios de use cases
├── domain/client/model/           # Tests de validación del modelo de dominio
└── infrastructure/rest/api/       # Tests de controllers (MockMvc + @WebMvcTest)
```

---

## � Eventos (Kafka)

Al crear una factura, se publica un evento `InvoiceCreatedEvent` en el topic `invoice.created` de Kafka. La publicación es **asíncrona** (`@Async`) y **no bloquea** el flujo principal — si Kafka no está disponible, la factura se crea igualmente.

- **Broker**: `localhost:29092` (host) / `kafka:9092` (contenedores)
- **Kafka UI**: `http://localhost:9090`
- **Docker Compose**: `kafka/docker-compose.yaml`

---

## �🐳 Docker

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

