package com.billMate.billing.application.useCase;

import com.billMate.billing.domain.client.model.Client;
import com.billMate.billing.domain.client.port.in.GetClientUseCase;
import com.billMate.billing.domain.client.port.out.ClientRepositoryPort;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Service
public class GetClientService implements GetClientUseCase {

    private static final Logger log = LoggerFactory.getLogger(GetClientService.class);
    private final ClientRepositoryPort clientRepositoryPort;

    public GetClientService(ClientRepositoryPort clientRepositoryPort) {
        this.clientRepositoryPort = clientRepositoryPort;
    }

    @Override
    public Client execute(Long clientId) {
        log.debug("Finding client", kv("clientId", clientId));
        return clientRepositoryPort.findById(clientId)
                .orElseThrow(() -> {
                    log.warn("Client not found", kv("clientId", clientId));
                    return new EntityNotFoundException("Client not found with ID: " + clientId);
                });
    }
}
