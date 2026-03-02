package com.billMate.billing.application.useCase;

import com.billMate.billing.domain.client.model.Client;
import com.billMate.billing.domain.client.port.in.GetAllClientsUseCase;
import com.billMate.billing.domain.client.port.out.ClientRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetAllClientsService implements GetAllClientsUseCase {

    private final ClientRepositoryPort clientRepositoryPort;

    public GetAllClientsService(ClientRepositoryPort clientRepositoryPort) {
        this.clientRepositoryPort = clientRepositoryPort;
    }

    @Override
    public List<Client> execute() {
        return clientRepositoryPort.findAll();
    }
}
