package com.billMate.billing.application.useCase;

import com.billMate.billing.domain.client.model.Client;
import com.billMate.billing.domain.client.port.in.CreateClientCommand;
import com.billMate.billing.domain.client.port.in.CreateClientUseCase;
import com.billMate.billing.domain.client.port.out.ClientRepositoryPort;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class CreateClientService implements CreateClientUseCase {

    private final ClientRepositoryPort clientRepositoryPort;

    public CreateClientService(ClientRepositoryPort clientRepositoryPort) {
        this.clientRepositoryPort = clientRepositoryPort;
    }

    @Override
    public Client execute(CreateClientCommand command) {
        Client client = new Client(
                null,
                command.name(),
                command.email(),
                command.phone(),
                command.nif(),
                command.address(),
                OffsetDateTime.now()
        );
        return clientRepositoryPort.save(client);
    }
}
