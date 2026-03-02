package com.billMate.billing.domain.invoice.port.in;

import java.time.LocalDate;
import java.util.List;

public record CreateInvoiceCommand(
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

    public CreateInvoiceCommand {
        if (clientId == null) {
            throw new IllegalArgumentException("Client ID is required");
        }
        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("Una factura debe tener al menos una línea.");
        }
    }
}
