package com.billMate.billing.domain.invoice.event;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

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
