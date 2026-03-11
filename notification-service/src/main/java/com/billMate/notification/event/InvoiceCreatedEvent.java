package com.billMate.notification.event;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Réplica del evento publicado por billing-service.
 * Se deserializa desde el topic Kafka "invoice.created".
 */
public record InvoiceCreatedEvent(
        Long invoiceId,
        Long clientId,
        LocalDate date,
        String status,
        String description,
        BigDecimal total,
        BigDecimal taxPercentage,
        LocalDateTime createdAt
) {
}
