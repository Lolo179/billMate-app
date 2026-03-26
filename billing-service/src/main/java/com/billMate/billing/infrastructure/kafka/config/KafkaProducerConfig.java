package com.billMate.billing.infrastructure.kafka.config;

import com.billMate.billing.domain.invoice.event.InvoiceCreatedEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.Map;

/**
 * Configuración explícita del productor Kafka para el servicio de facturación.
 * Solo se activa cuando Kafka está habilitado (billmate.kafka.enabled=true).
 * Define ProducerFactory y KafkaTemplate con tipos específicos para evitar
 * conflictos con la auto-configuración genérica de Spring Boot.
 */
@Configuration
@ConditionalOnProperty(name = "billmate.kafka.enabled", havingValue = "true")
public class KafkaProducerConfig {

    private final KafkaProperties kafkaProperties;

    public KafkaProducerConfig(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    @Bean
    public ProducerFactory<String, InvoiceCreatedEvent> producerFactory() {
        Map<String, Object> props = kafkaProperties.buildProducerProperties(null);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                org.apache.kafka.common.serialization.StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                org.springframework.kafka.support.serializer.JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, InvoiceCreatedEvent> kafkaTemplate(
            ProducerFactory<String, InvoiceCreatedEvent> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
