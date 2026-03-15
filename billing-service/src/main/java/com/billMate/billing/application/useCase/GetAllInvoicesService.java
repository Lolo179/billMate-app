package com.billMate.billing.application.useCase;

import com.billMate.billing.domain.invoice.model.Invoice;
import com.billMate.billing.domain.invoice.port.in.GetAllInvoicesUseCase;
import com.billMate.billing.domain.invoice.port.out.InvoiceRepositoryPort;
import com.billMate.billing.domain.shared.PageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Service
public class GetAllInvoicesService implements GetAllInvoicesUseCase {

    private static final int MAX_RESULTS = 20;
    private static final Logger log = LoggerFactory.getLogger(GetAllInvoicesService.class);
    private final InvoiceRepositoryPort invoiceRepositoryPort;

    public GetAllInvoicesService(InvoiceRepositoryPort invoiceRepositoryPort) {
        this.invoiceRepositoryPort = invoiceRepositoryPort;
    }

    @Override
    public PageResult<Invoice> execute(int page, int size) {
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = Math.min(Math.max(size, 1), MAX_RESULTS);

        log.debug("Fetching invoices", kv("page", normalizedPage), kv("size", normalizedSize));
        PageResult<Invoice> invoices = invoiceRepositoryPort.findAll(normalizedPage, normalizedSize);
        log.debug("Invoices found", kv("count", invoices.items().size()), kv("totalElements", invoices.totalElements()));
        return invoices;
    }
}
