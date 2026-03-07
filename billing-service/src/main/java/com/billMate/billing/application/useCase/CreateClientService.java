package com.billMate.billing.application.useCase;

import com.billMate.billing.domain.client.model.Client;
import com.billMate.billing.domain.client.port.in.CreateClientCommand;
import com.billMate.billing.domain.client.port.in.CreateClientUseCase;
import com.billMate.billing.domain.client.port.out.ClientRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Service
public class CreateClientService implements CreateClientUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreateClientService.class);
    private final ClientRepositoryPort clientRepositoryPort;

    public CreateClientService(ClientRepositoryPort clientRepositoryPort) {
        this.clientRepositoryPort = clientRepositoryPort;
    }

    @Override
    public Client execute(CreateClientCommand command) {
        log.info("Creating client", kv("nif", command.nif()), kv("name", command.name()));
        Client client = new Client(
                null,
                command.name(),
                command.email(),
                command.phone(),
                command.nif(),
                command.address(),
                OffsetDateTime.now()
        );
        Client saved = clientRepositoryPort.save(client);
        log.info("Client created", kv("clientId", saved.getId()));
        return saved;
    }
}
