# Notification Service – Consumidor de Eventos Kafka

## Descripción

Servicio **ficticio** de notificaciones por email. Consume el evento `InvoiceCreatedEvent` del topic `invoice.created` publicado por billing-service y simula el envío de un email logueando la información.

**No tiene base de datos.** Su único propósito es demostrar el consumo de eventos Kafka.

## Estructura

```
com.billMate.notification
├── NotificationServiceApplication.java   # @SpringBootApplication
├── event/
│   └── InvoiceCreatedEvent.java          # record — réplica del evento de billing
└── listener/
    └── InvoiceCreatedListener.java       # @KafkaListener — consume y loguea
```

## Configuración Kafka

- **Topic**: `invoice.created`
- **Group ID**: `notification-service`
- **Bootstrap servers**: `localhost:29092`
- **Deserializer**: `JsonDeserializer` con trusted packages `com.billMate.*`
- **Auto offset reset**: `earliest`
- **Type mapping**: `com.billMate.billing.domain.invoice.event.InvoiceCreatedEvent` → `com.billMate.notification.event.InvoiceCreatedEvent` (mapea la clase del productor a la réplica local, evitando errores de deserialización por `__TypeId__`)

## Puerto

`8084`

## Observabilidad

### Logging Estructurado

`logback-spring.xml` con `LogstashEncoder` (JSON). Mismo formato que el resto de servicios.

```java
import static net.logstash.logback.argument.StructuredArguments.kv;

log.info("Received invoice.created event", kv("invoiceId", event.invoiceId()));
log.info("[EMAIL SIMULATION] Sending email notification...", kv("invoiceId", event.invoiceId()));
log.info("[EMAIL SIMULATION] Email sent successfully", kv("to", "client-X@billmate.test"));
```

## Testing

- `@SpringBootTest` + `@EmbeddedKafka` para test de contexto
- Sin Testcontainers (no hay BD)

## Docker

Dockerfile multi-stage (eclipse-temurin:21 Alpine). Puerto `8084`.
