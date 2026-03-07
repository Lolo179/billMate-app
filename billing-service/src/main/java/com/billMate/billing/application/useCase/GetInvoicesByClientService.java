package com.billMate.billing.application.useCase;

import com.billMate.billing.domain.invoice.model.Invoice;
import com.billMate.billing.domain.invoice.port.in.GetInvoicesByClientUseCase;
import com.billMate.billing.domain.invoice.port.out.InvoiceRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Service
public class GetInvoicesByClientService implements GetInvoicesByClientUseCase {

    private static final Logger log = LoggerFactory.getLogger(GetInvoicesByClientService.class);
    private final InvoiceRepositoryPort invoiceRepositoryPort;

    public GetInvoicesByClientService(InvoiceRepositoryPort invoiceRepositoryPort) {
        this.invoiceRepositoryPort = invoiceRepositoryPort;
    }

    @Override
    public List<Invoice> execute(Long clientId) {
        log.debug("Fetching invoices by client", kv("clientId", clientId));
        List<Invoice> invoices = invoiceRepositoryPort.findAllByClientId(clientId);
        log.debug("Invoices found for client", kv("clientId", clientId), kv("count", invoices.size()));
        return invoices;
    }
}
