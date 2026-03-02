package com.billMate.billing.application.useCase;

import com.billMate.billing.domain.client.port.in.DeleteClientUseCase;
import com.billMate.billing.domain.client.port.out.ClientRepositoryPort;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DeleteClientService implements DeleteClientUseCase {

    private final ClientRepositoryPort clientRepositoryPort;

    public DeleteClientService(ClientRepositoryPort clientRepositoryPort) {
        this.clientRepositoryPort = clientRepositoryPort;
    }

    @Override
    public void execute(Long clientId) {
        if (!clientRepositoryPort.existsById(clientId)) {
            throw new EntityNotFoundException("Client not found with ID: " + clientId);
        }
        clientRepositoryPort.deleteById(clientId);
    }
}
