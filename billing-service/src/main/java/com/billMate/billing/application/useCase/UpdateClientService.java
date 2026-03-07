package com.billMate.billing.application.useCase;

import com.billMate.billing.domain.client.model.Client;
import com.billMate.billing.domain.client.port.in.UpdateClientCommand;
import com.billMate.billing.domain.client.port.in.UpdateClientUseCase;
import com.billMate.billing.domain.client.port.out.ClientRepositoryPort;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Service
public class UpdateClientService implements UpdateClientUseCase {

    private static final Logger log = LoggerFactory.getLogger(UpdateClientService.class);
    private final ClientRepositoryPort clientRepositoryPort;

    public UpdateClientService(ClientRepositoryPort clientRepositoryPort) {
        this.clientRepositoryPort = clientRepositoryPort;
    }

    @Override
    public Client execute(UpdateClientCommand command) {
        log.info("Updating client", kv("clientId", command.clientId()));
        Client existing = clientRepositoryPort.findById(command.clientId())
                .orElseThrow(() -> {
                    log.warn("Client not found", kv("clientId", command.clientId()));
                    return new EntityNotFoundException("Client not found with ID: " + command.clientId());
                });

        existing.setName(command.name());
        existing.setEmail(command.email());
        existing.setPhone(command.phone());
        existing.setNif(command.nif());
        existing.setAddress(command.address());
        return clientRepositoryPort.save(existing);
    }
}
