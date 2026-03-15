package com.billMate.billing.application.useCase;

import com.billMate.billing.domain.client.model.Client;
import com.billMate.billing.domain.client.port.in.GetAllClientsUseCase;
import com.billMate.billing.domain.client.port.out.ClientRepositoryPort;
import com.billMate.billing.domain.shared.PageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Service
public class GetAllClientsService implements GetAllClientsUseCase {

    private static final int MAX_RESULTS = 20;
    private static final Logger log = LoggerFactory.getLogger(GetAllClientsService.class);
    private final ClientRepositoryPort clientRepositoryPort;

    public GetAllClientsService(ClientRepositoryPort clientRepositoryPort) {
        this.clientRepositoryPort = clientRepositoryPort;
    }

    @Override
    public PageResult<Client> execute(int page, int size) {
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = Math.min(Math.max(size, 1), MAX_RESULTS);

        log.debug("Fetching clients", kv("page", normalizedPage), kv("size", normalizedSize));
        PageResult<Client> clients = clientRepositoryPort.findAll(normalizedPage, normalizedSize);
        log.debug("Clients found", kv("count", clients.items().size()), kv("totalElements", clients.totalElements()));
        return clients;
    }
}
