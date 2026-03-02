package com.billMate.billing.application.useCase;

import com.billMate.billing.domain.client.model.Client;
import com.billMate.billing.domain.invoice.model.Invoice;
import com.billMate.billing.domain.invoice.model.InvoiceStatus;
import com.billMate.billing.domain.invoice.port.in.DownloadInvoicePdfUseCase;
import com.billMate.billing.domain.client.port.out.ClientRepositoryPort;
import com.billMate.billing.domain.invoice.port.out.InvoiceRepositoryPort;
import com.billMate.billing.domain.invoice.port.out.PdfGeneratorPort;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DownloadInvoicePdfService implements DownloadInvoicePdfUseCase {

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
        Invoice invoice = invoiceRepositoryPort.findById(invoiceId)
                .orElseThrow(() -> new EntityNotFoundException("Factura no encontrada"));

        if (invoice.getStatus() != InvoiceStatus.SENT && invoice.getStatus() != InvoiceStatus.PAID) {
            throw new IllegalStateException("Solo se puede descargar el PDF de facturas emitidas.");
        }

        Client client = clientRepositoryPort.findById(invoice.getClientId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado"));

        return pdfGeneratorPort.generate(invoice, client);
    }
}
