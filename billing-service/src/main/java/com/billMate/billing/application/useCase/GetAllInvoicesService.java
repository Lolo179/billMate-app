package com.billMate.billing.application.useCase;

import com.billMate.billing.domain.invoice.model.Invoice;
import com.billMate.billing.domain.invoice.model.InvoiceStatus;
import com.billMate.billing.domain.invoice.port.in.GetAllInvoicesUseCase;
import com.billMate.billing.domain.invoice.port.in.query.InvoiceSearchQuery;
import com.billMate.billing.domain.invoice.port.out.InvoiceRepositoryPort;
import com.billMate.billing.domain.shared.PageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Set;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Service
public class GetAllInvoicesService implements GetAllInvoicesUseCase {

    private static final int MAX_RESULTS = 20;
    private static final Logger log = LoggerFactory.getLogger(GetAllInvoicesService.class);

    // Whitelist para prevenir inyección SQL a través del parámetro sort
    private static final Set<String> VALID_SORT_FIELDS = Set.of("date", "total", "status", "createdAt");
    private static final String DEFAULT_SORT_FIELD = "createdAt";

    private final InvoiceRepositoryPort invoiceRepositoryPort;

    public GetAllInvoicesService(InvoiceRepositoryPort invoiceRepositoryPort) {
        this.invoiceRepositoryPort = invoiceRepositoryPort;
    }

    @Override
    public PageResult<Invoice> execute(InvoiceSearchQuery query) {
        int normalizedPage = Math.max(query.page(), 0);
        int normalizedSize = Math.min(Math.max(query.size(), 1), MAX_RESULTS);
        String sortField = resolveSortField(query.sortField());
        String sortDir = resolveSortDir(query.sortDir());

        log.debug("Fetching invoices",
                kv("page", normalizedPage), kv("size", normalizedSize),
                kv("sort", sortField + "," + sortDir),
                kv("status", query.status()));

        InvoiceSearchQuery normalizedQuery = new InvoiceSearchQuery(
                normalizedPage, normalizedSize, sortField, sortDir,
                query.status(), query.dateFrom(), query.dateTo(), null);

        PageResult<Invoice> invoices = invoiceRepositoryPort.search(normalizedQuery);
        log.debug("Invoices found",
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
