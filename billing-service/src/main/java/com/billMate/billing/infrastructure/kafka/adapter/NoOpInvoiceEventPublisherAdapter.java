package com.billMate.billing.infrastructure.kafka.adapter;

import com.billMate.billing.domain.invoice.event.InvoiceCreatedEvent;
import com.billMate.billing.domain.invoice.port.out.InvoiceEventPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import static net.logstash.logback.argument.StructuredArguments.kv;

/**
 * Adaptador vacío de publicación de eventos. Se activa cuando Kafka está deshabilitado
 * (billmate.kafka.enabled=false). Registra un warn y descarta el evento sin error.
 */
@Component
@ConditionalOnProperty(name = "billmate.kafka.enabled", havingValue = "false", matchIfMissing = true)
public class NoOpInvoiceEventPublisherAdapter implements InvoiceEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(NoOpInvoiceEventPublisherAdapter.class);

    @Override
    public void publishInvoiceCreated(InvoiceCreatedEvent event) {
        log.warn("Kafka disabled — invoice created event discarded", kv("invoiceId", event.invoiceId()));
    }
}
