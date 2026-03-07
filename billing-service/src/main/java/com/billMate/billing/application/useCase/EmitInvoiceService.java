package com.billMate.billing.application.useCase;

import com.billMate.billing.domain.client.model.Client;
import com.billMate.billing.domain.invoice.model.Invoice;
import com.billMate.billing.domain.invoice.model.InvoiceStatus;
import com.billMate.billing.domain.invoice.port.in.EmitInvoiceUseCase;
import com.billMate.billing.domain.client.port.out.ClientRepositoryPort;
import com.billMate.billing.domain.invoice.port.out.InvoiceRepositoryPort;
import com.billMate.billing.domain.invoice.port.out.PdfGeneratorPort;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Service
public class EmitInvoiceService implements EmitInvoiceUseCase {

    private static final Logger log = LoggerFactory.getLogger(EmitInvoiceService.class);
    private final InvoiceRepositoryPort invoiceRepositoryPort;
    private final ClientRepositoryPort clientRepositoryPort;
    private final PdfGeneratorPort pdfGeneratorPort;

    public EmitInvoiceService(InvoiceRepositoryPort invoiceRepositoryPort,
                              ClientRepositoryPort clientRepositoryPort,
                              PdfGeneratorPort pdfGeneratorPort) {
        this.invoiceRepositoryPort = invoiceRepositoryPort;
        this.clientRepositoryPort = clientRepositoryPort;
        this.pdfGeneratorPort = pdfGeneratorPort;
    }

    @Override
    public byte[] execute(Long invoiceId) {
        log.info("Emitting invoice", kv("invoiceId", invoiceId));
        Invoice invoice = invoiceRepositoryPort.findById(invoiceId)
                .orElseThrow(() -> {
                    log.warn("Invoice not found for emission", kv("invoiceId", invoiceId));
                    return new EntityNotFoundException("Factura no encontrada");
                });

        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            log.warn("Cannot emit invoice: invalid status", kv("invoiceId", invoiceId), kv("status", invoice.getStatus()));
            throw new IllegalStateException("Solo las facturas en estado DRAFT pueden ser emitidas.");
        }

        if (invoice.getDate() == null) {
            invoice.setDate(LocalDate.now());
        }

        invoice.setStatus(InvoiceStatus.SENT);
        Invoice emitted = invoiceRepositoryPort.save(invoice);
        log.info("Invoice emitted", kv("invoiceId", invoiceId), kv("status", "SENT"));

        Client client = clientRepositoryPort.findById(emitted.getClientId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado"));

        return pdfGeneratorPort.generate(emitted, client);
    }
}
