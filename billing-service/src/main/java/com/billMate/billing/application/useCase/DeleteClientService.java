package com.billMate.billing.application.useCase;

import com.billMate.billing.domain.client.port.in.DeleteClientUseCase;
import com.billMate.billing.domain.client.port.out.ClientRepositoryPort;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Service
public class DeleteClientService implements DeleteClientUseCase {

    private static final Logger log = LoggerFactory.getLogger(DeleteClientService.class);
    private final ClientRepositoryPort clientRepositoryPort;

    public DeleteClientService(ClientRepositoryPort clientRepositoryPort) {
        this.clientRepositoryPort = clientRepositoryPort;
    }

    @Override
    public void execute(Long clientId) {
        log.info("Deleting client", kv("clientId", clientId));
        if (!clientRepositoryPort.existsById(clientId)) {
            log.warn("Client not found for deletion", kv("clientId", clientId));
            throw new EntityNotFoundException("Client not found with ID: " + clientId);
        }
        clientRepositoryPort.deleteById(clientId);
        log.info("Client deleted", kv("clientId", clientId));
    }
}
