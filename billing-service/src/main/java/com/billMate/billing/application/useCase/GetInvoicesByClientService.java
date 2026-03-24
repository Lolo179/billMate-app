package com.billMate.billing.application.useCase;

import com.billMate.billing.domain.client.port.out.ClientRepositoryPort;
import com.billMate.billing.domain.invoice.model.Invoice;
import com.billMate.billing.domain.invoice.port.in.GetInvoicesByClientUseCase;
import com.billMate.billing.domain.invoice.port.in.query.InvoiceSearchQuery;
import com.billMate.billing.domain.invoice.port.out.InvoiceRepositoryPort;
import com.billMate.billing.domain.shared.PageResult;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Set;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Service
public class GetInvoicesByClientService implements GetInvoicesByClientUseCase {

    private static final int MAX_RESULTS = 20;
    private static final Logger log = LoggerFactory.getLogger(GetInvoicesByClientService.class);

    private static final Set<String> VALID_SORT_FIELDS = Set.of("date", "total", "status", "createdAt");
    private static final String DEFAULT_SORT_FIELD = "createdAt";

    private final InvoiceRepositoryPort invoiceRepositoryPort;
    private final ClientRepositoryPort clientRepositoryPort;

    public GetInvoicesByClientService(InvoiceRepositoryPort invoiceRepositoryPort,
                                     ClientRepositoryPort clientRepositoryPort) {
        this.invoiceRepositoryPort = invoiceRepositoryPort;
        this.clientRepositoryPort = clientRepositoryPort;
    }

    @Override
    public PageResult<Invoice> execute(InvoiceSearchQuery query) {
        int normalizedPage = Math.max(query.page(), 0);
        int normalizedSize = Math.min(Math.max(query.size(), 1), MAX_RESULTS);
        String sortField = resolveSortField(query.sortField());
        String sortDir = resolveSortDir(query.sortDir());

        log.debug("Fetching invoices by client",
                kv("clientId", query.clientId()),
                kv("page", normalizedPage), kv("size", normalizedSize),
                kv("sort", sortField + "," + sortDir),
                kv("status", query.status()));

        clientRepositoryPort.findById(query.clientId())
                .orElseThrow(() -> {
                    log.warn("Client not found", kv("clientId", query.clientId()));
                    return new EntityNotFoundException(
                            "Cliente con ID " + query.clientId() + " no encontrado.");
                });

        InvoiceSearchQuery normalizedQuery = new InvoiceSearchQuery(
                normalizedPage, normalizedSize, sortField, sortDir,
                query.status(), query.dateFrom(), query.dateTo(), query.clientId());

        PageResult<Invoice> invoices = invoiceRepositoryPort.search(normalizedQuery);
        log.debug("Invoices found for client",
                kv("clientId", query.clientId()),
                kv("count", invoices.items().size()), kv("totalElements", invoices.totalElements()));
        return invoices;
    }

    private String resolveSortField(String rawSortField) {
        if (rawSortField == null || rawSortField.isBlank()) {
            return DEFAULT_SORT_FIELD;
        }
        String field = rawSortField.split(",")[0].trim();
        if (!VALID_SORT_FIELDS.contains(field)) {
            throw new IllegalArgumentException(
                    "Campo de ordenación inválido: '" + field + "'. Valores permitidos: " + VALID_SORT_FIELDS);
        }
        return field;
    }

    private String resolveSortDir(String rawSortDir) {
        if (rawSortDir == null || rawSortDir.isBlank()) {
            return "desc";
        }
        return "asc".equalsIgnoreCase(rawSortDir) ? "asc" : "desc";
    }
}
