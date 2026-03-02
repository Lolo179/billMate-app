package com.billMate.billing.application.useCase;

import com.billMate.billing.domain.invoice.port.in.DeleteInvoiceUseCase;
import com.billMate.billing.domain.invoice.port.out.InvoiceRepositoryPort;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DeleteInvoiceService implements DeleteInvoiceUseCase {

    private final InvoiceRepositoryPort invoiceRepositoryPort;

    public DeleteInvoiceService(InvoiceRepositoryPort invoiceRepositoryPort) {
        this.invoiceRepositoryPort = invoiceRepositoryPort;
    }

    @Override
    public void execute(Long invoiceId) {
        if (!invoiceRepositoryPort.existsById(invoiceId)) {
            throw new EntityNotFoundException("Factura no encontrada");
        }
        invoiceRepositoryPort.deleteById(invoiceId);
    }
}
