package com.billMate.billing.application.useCase;

import com.billMate.billing.domain.client.model.Client;
import com.billMate.billing.domain.client.port.in.GetAllClientsUseCase;
import com.billMate.billing.domain.client.port.out.ClientRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Service
public class GetAllClientsService implements GetAllClientsUseCase {

    private static final Logger log = LoggerFactory.getLogger(GetAllClientsService.class);
    private final ClientRepositoryPort clientRepositoryPort;

    public GetAllClientsService(ClientRepositoryPort clientRepositoryPort) {
        this.clientRepositoryPort = clientRepositoryPort;
    }

    @Override
    public List<Client> execute() {
        log.debug("Fetching all clients");
        List<Client> clients = clientRepositoryPort.findAll();
        log.debug("Clients found", kv("count", clients.size()));
        return clients;
    }
}
