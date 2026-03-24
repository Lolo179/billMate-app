package com.billMate.billing.application.useCase;

import com.billMate.billing.domain.invoice.model.Invoice;
import com.billMate.billing.domain.invoice.model.InvoiceLineItem;
import com.billMate.billing.domain.invoice.model.InvoiceStatus;
import com.billMate.billing.domain.invoice.port.in.PatchInvoiceUseCase;
import com.billMate.billing.domain.invoice.port.in.command.PatchInvoiceCommand;
import com.billMate.billing.domain.invoice.port.out.InvoiceRepositoryPort;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

import static net.logstash.logback.argument.StructuredArguments.kv;

/**
 * Servicio de actualización parcial de factura (JSON Merge Patch, RFC 7396).
 * Solo aplica los campos presentes en el comando (Optional.isPresent()).
 * Solo se pueden actualizar facturas en estado DRAFT.
 */
@Service
public class PatchInvoiceService implements PatchInvoiceUseCase {

    private static final Logger log = LoggerFactory.getLogger(PatchInvoiceService.class);
    private final InvoiceRepositoryPort invoiceRepositoryPort;

    public PatchInvoiceService(InvoiceRepositoryPort invoiceRepositoryPort) {
        this.invoiceRepositoryPort = invoiceRepositoryPort;
    }

    @Override
    public Invoice execute(PatchInvoiceCommand command) {
        log.info("Patching invoice", kv("invoiceId", command.invoiceId()));

        Invoice invoice = invoiceRepositoryPort.findById(command.invoiceId())
                .orElseThrow(() -> {
                    log.warn("Invoice not found for patch", kv("invoiceId", command.invoiceId()));
                    return new EntityNotFoundException("Factura con ID " + command.invoiceId() + " no encontrada.");
                });

        // Solo se puede editar una factura en estado DRAFT
        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            log.warn("Patch denied: invoice is not in DRAFT state",
                    kv("invoiceId", command.invoiceId()), kv("status", invoice.getStatus()));
            throw new IllegalStateException(
                    "Solo se pueden actualizar facturas en estado DRAFT. Estado actual: " + invoice.getStatus());
        }

        command.clientId().ifPresent(invoice::setClientId);
        command.date().ifPresent(invoice::setDate);
        command.description().ifPresent(invoice::setDescription);
        command.lines().ifPresent(lines -> {
            List<InvoiceLineItem> lineItems = lines.stream()
                    .map(l -> new InvoiceLineItem(
                            null,
                            l.description(),
                            BigDecimal.valueOf(l.quantity()),
                            BigDecimal.valueOf(l.unitPrice()),
                            null))   // total se calcula automáticamente en el constructor
                    .toList();
            invoice.setLines(lineItems);
            invoice.recalculateTotal();
        });

        Invoice updated = invoiceRepositoryPort.save(invoice);
        log.info("Invoice patched", kv("invoiceId", updated.getId()));
        return updated;
    }
}
