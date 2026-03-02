package com.billMate.billing.application.useCase;

import com.billMate.billing.domain.client.model.Client;
import com.billMate.billing.domain.client.port.in.GetClientUseCase;
import com.billMate.billing.domain.client.port.out.ClientRepositoryPort;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class GetClientService implements GetClientUseCase {

    private final ClientRepositoryPort clientRepositoryPort;

    public GetClientService(ClientRepositoryPort clientRepositoryPort) {
        this.clientRepositoryPort = clientRepositoryPort;
    }

    @Override
    public Client execute(Long clientId) {
        return clientRepositoryPort.findById(clientId)
                .orElseThrow(() -> new EntityNotFoundException("Client not found with ID: " + clientId));
    }
}
