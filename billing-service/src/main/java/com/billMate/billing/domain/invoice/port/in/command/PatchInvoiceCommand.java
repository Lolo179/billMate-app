package com.billMate.billing.domain.invoice.port.in.command;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Comando para actualización parcial de una factura (JSON Merge Patch, RFC 7396).
 * Solo válido para facturas en estado DRAFT.
 * Un Optional.empty() indica que el campo no está presente en el payload y no debe modificarse.
 */
public record PatchInvoiceCommand(
        Long invoiceId,
        Optional<Long> clientId,
        Optional<LocalDate> date,
        Optional<String> description,
        Optional<List<LineCommand>> lines) {

    public record LineCommand(String description, Double quantity, Double unitPrice) {
    }
}
