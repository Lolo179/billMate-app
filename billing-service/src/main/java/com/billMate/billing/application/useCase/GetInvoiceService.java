package com.billMate.billing.application.useCase;

import com.billMate.billing.domain.invoice.model.Invoice;
import com.billMate.billing.domain.invoice.port.in.GetInvoiceUseCase;
import com.billMate.billing.domain.invoice.port.out.InvoiceRepositoryPort;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class GetInvoiceService implements GetInvoiceUseCase {

    private final InvoiceRepositoryPort invoiceRepositoryPort;

    public GetInvoiceService(InvoiceRepositoryPort invoiceRepositoryPort) {
        this.invoiceRepositoryPort = invoiceRepositoryPort;
    }

    @Override
    public Invoice execute(Long invoiceId) {
        return invoiceRepositoryPort.findById(invoiceId)
                .orElseThrow(() -> new EntityNotFoundException("Factura no encontrada"));
    }
}
