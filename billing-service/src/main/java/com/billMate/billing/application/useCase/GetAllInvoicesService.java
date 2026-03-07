package com.billMate.billing.application.useCase;

import com.billMate.billing.domain.invoice.model.Invoice;
import com.billMate.billing.domain.invoice.port.in.GetAllInvoicesUseCase;
import com.billMate.billing.domain.invoice.port.out.InvoiceRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Service
public class GetAllInvoicesService implements GetAllInvoicesUseCase {

    private static final Logger log = LoggerFactory.getLogger(GetAllInvoicesService.class);
    private final InvoiceRepositoryPort invoiceRepositoryPort;

    public GetAllInvoicesService(InvoiceRepositoryPort invoiceRepositoryPort) {
        this.invoiceRepositoryPort = invoiceRepositoryPort;
    }

    @Override
    public List<Invoice> execute() {
        log.debug("Fetching all invoices");
        List<Invoice> invoices = invoiceRepositoryPort.findAll();
        log.debug("Invoices found", kv("count", invoices.size()));
        return invoices;
    }
}
