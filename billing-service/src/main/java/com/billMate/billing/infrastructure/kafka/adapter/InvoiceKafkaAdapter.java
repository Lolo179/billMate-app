package com.billMate.billing.infrastructure.kafka.adapter;

import com.billMate.billing.domain.invoice.event.InvoiceCreatedEvent;
import com.billMate.billing.domain.invoice.port.out.InvoiceEventPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Component
@ConditionalOnProperty(name = "billmate.kafka.enabled", havingValue = "true")
public class InvoiceKafkaAdapter implements InvoiceEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(InvoiceKafkaAdapter.class);
    private static final String TOPIC = "invoice.created";

    private final KafkaTemplate<String, InvoiceCreatedEvent> kafkaTemplate;

    public InvoiceKafkaAdapter(KafkaTemplate<String, InvoiceCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Async
    @Override
    public void publishInvoiceCreated(InvoiceCreatedEvent event) {
        try {
            log.info("Publishing invoice created event", kv("invoiceId", event.invoiceId()), kv("topic", TOPIC));
            kafkaTemplate.send(TOPIC, String.valueOf(event.invoiceId()), event);
        } catch (Exception e) {
            log.error("Failed to publish invoice created event", kv("invoiceId", event.invoiceId()), e);
        }
    }
}
