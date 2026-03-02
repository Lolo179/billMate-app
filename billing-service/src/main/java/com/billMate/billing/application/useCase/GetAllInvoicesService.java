package com.billMate.billing.application.useCase;

import com.billMate.billing.domain.invoice.model.Invoice;
import com.billMate.billing.domain.invoice.port.in.GetAllInvoicesUseCase;
import com.billMate.billing.domain.invoice.port.out.InvoiceRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetAllInvoicesService implements GetAllInvoicesUseCase {

    private final InvoiceRepositoryPort invoiceRepositoryPort;

    public GetAllInvoicesService(InvoiceRepositoryPort invoiceRepositoryPort) {
        this.invoiceRepositoryPort = invoiceRepositoryPort;
    }

    @Override
    public List<Invoice> execute() {
        return invoiceRepositoryPort.findAll();
    }
}
