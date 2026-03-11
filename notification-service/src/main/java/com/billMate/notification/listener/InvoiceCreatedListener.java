package com.billMate.notification.listener;

import com.billMate.notification.event.InvoiceCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static net.logstash.logback.argument.StructuredArguments.kv;

/**
 * Consumidor Kafka que simula el envío de notificaciones por email
 * cuando se crea una factura en billing-service.
 */
@Component
public class InvoiceCreatedListener {

    private static final Logger log = LoggerFactory.getLogger(InvoiceCreatedListener.class);

    @KafkaListener(topics = "invoice.created", groupId = "notification-service")
    public void onInvoiceCreated(InvoiceCreatedEvent event) {
        log.info("Received invoice.created event",
                kv("invoiceId", event.invoiceId()),
                kv("clientId", event.clientId()),
                kv("total", event.total()));

        log.info("[EMAIL SIMULATION] Sending email notification for new invoice",
                kv("invoiceId", event.invoiceId()),
                kv("clientId", event.clientId()),
                kv("status", event.status()),
                kv("total", event.total()),
                kv("taxPercentage", event.taxPercentage()),
                kv("date", event.date()),
                kv("description", event.description()));

        log.info("[EMAIL SIMULATION] Email sent successfully",
                kv("invoiceId", event.invoiceId()),
                kv("to", "client-" + event.clientId() + "@billmate.test"));
    }
}
