package com.billMate.billing.application.useCase;

import com.billMate.billing.domain.client.model.Client;
import com.billMate.billing.domain.invoice.model.Invoice;
import com.billMate.billing.domain.invoice.model.InvoiceStatus;
import com.billMate.billing.domain.invoice.port.in.EmitInvoiceUseCase;
import com.billMate.billing.domain.client.port.out.ClientRepositoryPort;
import com.billMate.billing.domain.invoice.port.out.InvoiceRepositoryPort;
import com.billMate.billing.domain.invoice.port.out.PdfGeneratorPort;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class EmitInvoiceService implements EmitInvoiceUseCase {

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
        Invoice invoice = invoiceRepositoryPort.findById(invoiceId)
                .orElseThrow(() -> new EntityNotFoundException("Factura no encontrada"));

        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new IllegalStateException("Solo las facturas en estado DRAFT pueden ser emitidas.");
        }

        if (invoice.getDate() == null) {
            invoice.setDate(LocalDate.now());
        }

        invoice.setStatus(InvoiceStatus.SENT);
        Invoice emitted = invoiceRepositoryPort.save(invoice);

        Client client = clientRepositoryPort.findById(emitted.getClientId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado"));

        return pdfGeneratorPort.generate(emitted, client);
    }
}
