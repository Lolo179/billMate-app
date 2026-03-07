package com.billMate.billing.application.useCase;

import com.billMate.billing.domain.client.model.Client;
import com.billMate.billing.domain.invoice.model.Invoice;
import com.billMate.billing.domain.invoice.model.InvoiceStatus;
import com.billMate.billing.domain.invoice.port.in.DownloadInvoicePdfUseCase;
import com.billMate.billing.domain.client.port.out.ClientRepositoryPort;
import com.billMate.billing.domain.invoice.port.out.InvoiceRepositoryPort;
import com.billMate.billing.domain.invoice.port.out.PdfGeneratorPort;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Service
public class DownloadInvoicePdfService implements DownloadInvoicePdfUseCase {

    private static final Logger log = LoggerFactory.getLogger(DownloadInvoicePdfService.class);
    private final InvoiceRepositoryPort invoiceRepositoryPort;
    private final ClientRepositoryPort clientRepositoryPort;
    private final PdfGeneratorPort pdfGeneratorPort;

    public DownloadInvoicePdfService(InvoiceRepositoryPort invoiceRepositoryPort,
                                     ClientRepositoryPort clientRepositoryPort,
                                     PdfGeneratorPort pdfGeneratorPort) {
        this.invoiceRepositoryPort = invoiceRepositoryPort;
        this.clientRepositoryPort = clientRepositoryPort;
        this.pdfGeneratorPort = pdfGeneratorPort;
    }

    @Override
    public byte[] execute(Long invoiceId) {
        log.info("Downloading invoice PDF", kv("invoiceId", invoiceId));
        Invoice invoice = invoiceRepositoryPort.findById(invoiceId)
                .orElseThrow(() -> {
                    log.warn("Invoice not found for PDF download", kv("invoiceId", invoiceId));
                    return new EntityNotFoundException("Factura no encontrada");
                });

        if (invoice.getStatus() != InvoiceStatus.SENT && invoice.getStatus() != InvoiceStatus.PAID) {
            log.warn("PDF download denied: invalid status", kv("invoiceId", invoiceId), kv("status", invoice.getStatus()));
            throw new IllegalStateException("Solo se puede descargar el PDF de facturas emitidas.");
        }

        Client client = clientRepositoryPort.findById(invoice.getClientId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado"));

        log.info("Generating PDF", kv("invoiceId", invoiceId));
        return pdfGeneratorPort.generate(invoice, client);
    }
}
