package com.billMate.billing.application.useCase;

import com.billMate.billing.domain.invoice.model.Invoice;
import com.billMate.billing.domain.invoice.model.InvoiceStatus;
import com.billMate.billing.domain.invoice.port.in.PayInvoiceUseCase;
import com.billMate.billing.domain.invoice.port.out.InvoiceRepositoryPort;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Service
public class PayInvoiceService implements PayInvoiceUseCase {

    private static final Logger log = LoggerFactory.getLogger(PayInvoiceService.class);
    private final InvoiceRepositoryPort invoiceRepositoryPort;

    public PayInvoiceService(InvoiceRepositoryPort invoiceRepositoryPort) {
        this.invoiceRepositoryPort = invoiceRepositoryPort;
    }

    @Override
    public Invoice execute(Long invoiceId) {
        log.info("Marking invoice as paid", kv("invoiceId", invoiceId));
        Invoice invoice = invoiceRepositoryPort.findById(invoiceId)
                .orElseThrow(() -> {
                    log.warn("Invoice not found for payment", kv("invoiceId", invoiceId));
                    return new EntityNotFoundException("Factura no encontrada");
                });

        if (!invoice.getStatus().equals(InvoiceStatus.SENT)) {
            log.warn("Cannot pay invoice: invalid status", kv("invoiceId", invoiceId), kv("status", invoice.getStatus()));
            throw new IllegalStateException("Solo se pueden marcar como pagadas las facturas en estado SENT.");
        }

        invoice.setStatus(InvoiceStatus.PAID);
        Invoice paid = invoiceRepositoryPort.save(invoice);
        log.info("Invoice paid", kv("invoiceId", invoiceId));
        return paid;
    }
}
