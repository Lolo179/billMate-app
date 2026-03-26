package com.billMate.billing.infrastructure.kafka.config;

import com.billMate.billing.domain.invoice.event.InvoiceCreatedEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

/**
 * Configuración explícita del KafkaTemplate tipado para envío de eventos de facturación.
 * Spring Boot 4.x solo auto-configura KafkaTemplate&lt;Object, Object&gt;; este bean provee
 * el tipo concreto que requiere InvoiceKafkaAdapter.
 */
@Configuration
@ConditionalOnProperty(name = "billmate.kafka.enabled", havingValue = "true")
public class KafkaConfig {

    @Bean
    public KafkaTemplate<String, InvoiceCreatedEvent> invoiceKafkaTemplate(
            ProducerFactory<String, InvoiceCreatedEvent> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
