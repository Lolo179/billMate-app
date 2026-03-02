package com.billMate.billing.application.useCase;

import com.billMate.billing.domain.invoice.model.Invoice;
import com.billMate.billing.domain.invoice.model.InvoiceStatus;
import com.billMate.billing.domain.invoice.port.in.PayInvoiceUseCase;
import com.billMate.billing.domain.invoice.port.out.InvoiceRepositoryPort;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class PayInvoiceService implements PayInvoiceUseCase {

    private final InvoiceRepositoryPort invoiceRepositoryPort;

    public PayInvoiceService(InvoiceRepositoryPort invoiceRepositoryPort) {
        this.invoiceRepositoryPort = invoiceRepositoryPort;
    }

    @Override
    public Invoice execute(Long invoiceId) {
        Invoice invoice = invoiceRepositoryPort.findById(invoiceId)
                .orElseThrow(() -> new EntityNotFoundException("Factura no encontrada"));

        if (!invoice.getStatus().equals(InvoiceStatus.SENT)) {
            throw new IllegalStateException("Solo se pueden marcar como pagadas las facturas en estado SENT.");
        }

        invoice.setStatus(InvoiceStatus.PAID);
        return invoiceRepositoryPort.save(invoice);
    }
}
