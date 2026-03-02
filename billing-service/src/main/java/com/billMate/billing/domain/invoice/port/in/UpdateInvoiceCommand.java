package com.billMate.billing.domain.invoice.port.in;

import java.time.LocalDate;
import java.util.List;

public record UpdateInvoiceCommand(
        Long invoiceId,
        Long clientId,
        LocalDate date,
        String description,
        String status,
        List<LineCommand> lines
) {
    public record LineCommand(
            String description,
            Double quantity,
            Double unitPrice
    ) {
    }
}
