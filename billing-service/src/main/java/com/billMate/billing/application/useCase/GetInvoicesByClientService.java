package com.billMate.billing.application.useCase;

import com.billMate.billing.domain.invoice.model.Invoice;
import com.billMate.billing.domain.invoice.port.in.GetInvoicesByClientUseCase;
import com.billMate.billing.domain.invoice.port.out.InvoiceRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetInvoicesByClientService implements GetInvoicesByClientUseCase {

    private final InvoiceRepositoryPort invoiceRepositoryPort;

    public GetInvoicesByClientService(InvoiceRepositoryPort invoiceRepositoryPort) {
        this.invoiceRepositoryPort = invoiceRepositoryPort;
    }

    @Override
    public List<Invoice> execute(Long clientId) {
        return invoiceRepositoryPort.findAllByClientId(clientId);
    }
}
