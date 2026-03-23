# Notification Service – BillMate

Microservicio **ficticio** de notificaciones por email. Consume eventos de **Apache Kafka** publicados por el billing-service y simula el envío de un email logueando la información.

> No tiene base de datos. Su propósito es demostrar el consumo asíncrono de eventos entre microservicios.

---

## 📬 Descripción

Este módulo:

- Se suscribe al topic `invoice.created` de Kafka
- Recibe el evento `InvoiceCreatedEvent` al crear una factura en billing-service
- Simula el envío de un email logueando los datos de la factura

---

## 🛠️ Stack Tecnológico

- Java 21 (LTS)
- Spring Boot 3.3.0
- Apache Kafka 3.8.0 (KRaft) + Spring Kafka
- Maven
- logstash-logback-encoder 7.4 (logs JSON estructurados)

---

## 🔧 Configuración por Defecto

El servicio se levanta en el puerto:

```
http://localhost:8084
```

Configuración en `src/main/resources/application.yaml`.

---

## 📨 Kafka

| Parámetro | Valor |
|---|---|
| Topic | `invoice.created` |
| Group ID | `notification-service` |
| Bootstrap servers | `localhost:29092` (host) / `kafka:9092` (contenedores) |
| Deserializador | `JsonDeserializer` |
| Auto offset reset | `earliest` |

### Type mapping

El productor (billing-service) serializa `com.billMate.billing.domain.invoice.event.InvoiceCreatedEvent`. Este servicio tiene su propia réplica en `com.billMate.notification.event.InvoiceCreatedEvent` y usa `spring.json.type.mapping` para evitar errores de deserialización por el header `__TypeId__`.

```bash
# Arrancar el broker Kafka (necesario para recibir eventos)
docker-compose -f ../kafka/docker-compose.yaml up -d
# Kafka UI disponible en http://localhost:9090
```

---

## 🧪 Testing

Test de contexto con `@SpringBootTest` + `@EmbeddedKafka`. No requiere Testcontainers (sin base de datos).

```bash
cd notification-service
mvn clean verify
```

---

## 📈 Observabilidad

Logs JSON estructurados con `logstash-logback-encoder`. Compatible con Grafana + Loki + Promtail:

```bash
docker-compose -f ../observability/docker-compose.yaml up -d   # Grafana en http://localhost:3000
```

Query LogQL de ejemplo: `{service="notification"} |= "invoice.created"`

---

## 🐳 Docker

```bash
docker build -t billmate/notification-service:latest .
```

---

## 📚 Referencias

- [BillMate Principal README](../README.md)
- [Billing Service](../billing-service/README.md) – Publica el evento `invoice.created`
- [Kafka](../kafka/) – Configuración del broker
