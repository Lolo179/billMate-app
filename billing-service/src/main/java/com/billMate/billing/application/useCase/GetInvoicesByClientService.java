package com.billMate.billing.application.useCase;

import com.billMate.billing.domain.invoice.model.Invoice;
import com.billMate.billing.domain.invoice.port.in.GetInvoicesByClientUseCase;
import com.billMate.billing.domain.client.port.out.ClientRepositoryPort;
import com.billMate.billing.domain.invoice.port.out.InvoiceRepositoryPort;
import com.billMate.billing.domain.shared.PageResult;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Service
public class GetInvoicesByClientService implements GetInvoicesByClientUseCase {

    private static final int MAX_RESULTS = 20;
    private static final Logger log = LoggerFactory.getLogger(GetInvoicesByClientService.class);
    private final InvoiceRepositoryPort invoiceRepositoryPort;
    private final ClientRepositoryPort clientRepositoryPort;

    public GetInvoicesByClientService(InvoiceRepositoryPort invoiceRepositoryPort,
                                     ClientRepositoryPort clientRepositoryPort) {
        this.invoiceRepositoryPort = invoiceRepositoryPort;
        this.clientRepositoryPort = clientRepositoryPort;
    }

    @Override
    public PageResult<Invoice> execute(Long clientId, int page, int size) {
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = Math.min(Math.max(size, 1), MAX_RESULTS);

        log.debug("Fetching invoices by client", kv("clientId", clientId), kv("page", normalizedPage), kv("size", normalizedSize));
        clientRepositoryPort.findById(clientId)
                .orElseThrow(() -> {
                    log.warn("Client not found", kv("clientId", clientId));
                    return new EntityNotFoundException(
                        "Cliente con ID " + clientId + " no encontrado.");
                });
        PageResult<Invoice> invoices = invoiceRepositoryPort.findAllByClientId(clientId, normalizedPage, normalizedSize);
        log.debug("Invoices found for client", kv("clientId", clientId), kv("count", invoices.items().size()), kv("totalElements", invoices.totalElements()));
        return invoices;
    }
}
