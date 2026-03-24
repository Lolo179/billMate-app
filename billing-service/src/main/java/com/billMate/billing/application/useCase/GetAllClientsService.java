package com.billMate.billing.application.useCase;

import com.billMate.billing.domain.client.model.Client;
import com.billMate.billing.domain.client.port.in.GetAllClientsUseCase;
import com.billMate.billing.domain.client.port.in.query.ClientSearchQuery;
import com.billMate.billing.domain.client.port.out.ClientRepositoryPort;
import com.billMate.billing.domain.shared.PageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Set;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Service
public class GetAllClientsService implements GetAllClientsUseCase {

    private static final int MAX_RESULTS = 20;
    private static final Logger log = LoggerFactory.getLogger(GetAllClientsService.class);

    // Whitelist de campos de ordenación para prevenir inyección SQL a través del parámetro sort
    private static final Set<String> VALID_SORT_FIELDS = Set.of("name", "email", "nif", "createdAt");
    private static final String DEFAULT_SORT_FIELD = "createdAt";

    private final ClientRepositoryPort clientRepositoryPort;

    public GetAllClientsService(ClientRepositoryPort clientRepositoryPort) {
        this.clientRepositoryPort = clientRepositoryPort;
    }

    @Override
    public PageResult<Client> execute(ClientSearchQuery query) {
        int normalizedPage = Math.max(query.page(), 0);
        int normalizedSize = Math.min(Math.max(query.size(), 1), MAX_RESULTS);
        String sortField = resolveSortField(query.sortField());
        String sortDir = resolveSortDir(query.sortDir());

        log.debug("Fetching clients",
                kv("page", normalizedPage), kv("size", normalizedSize),
                kv("sort", sortField + "," + sortDir),
                kv("name", query.name()), kv("nif", query.nif()));

        ClientSearchQuery normalizedQuery = new ClientSearchQuery(
                normalizedPage, normalizedSize, sortField, sortDir, query.name(), query.nif());

        PageResult<Client> clients = clientRepositoryPort.search(normalizedQuery);
        log.debug("Clients found",
                kv("count", clients.items().size()), kv("totalElements", clients.totalElements()));
        return clients;
    }

    /**
     * Valida el campo de ordenación contra la whitelist.
     * Si no es válido lanza IllegalArgumentException (→ 400 en GlobalExceptionHandler).
     */
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
