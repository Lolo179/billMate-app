package com.billMate.billing.infrastructure.rest.api;

import com.billMate.billing.domain.client.model.Client;
import com.billMate.billing.domain.client.port.in.*;
import com.billMate.billing.infrastructure.rest.dto.ClientDTO;
import com.billMate.billing.infrastructure.rest.dto.NewClientDTO;
import com.billMate.billing.infrastructure.rest.mapper.ClientRestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class ClientController implements ClientsApi {

    private final CreateClientUseCase createClientUseCase;
    private final GetClientUseCase getClientUseCase;
    private final GetAllClientsUseCase getAllClientsUseCase;
    private final UpdateClientUseCase updateClientUseCase;
    private final DeleteClientUseCase deleteClientUseCase;
    private final ClientRestMapper clientRestMapper;

    @Override
    public ResponseEntity<ClientDTO> createClient(NewClientDTO newClientDTO) {
        CreateClientCommand command = clientRestMapper.toCreateCommand(newClientDTO);
        Client client = createClientUseCase.execute(command);
        return ResponseEntity.status(201).body(clientRestMapper.toDto(client));
    }

    @Override
    public ResponseEntity<ClientDTO> getClientById(Long clientId) {
        Client client = getClientUseCase.execute(clientId);
        return ResponseEntity.ok(clientRestMapper.toDto(client));
    }

    @Override
    public ResponseEntity<List<ClientDTO>> getClients() {
        List<ClientDTO> clients = getAllClientsUseCase.execute().stream()
                .map(clientRestMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(clients);
    }

    @Override
    public ResponseEntity<ClientDTO> updateClient(Long clientId, NewClientDTO newClientDTO) {
        UpdateClientCommand command = clientRestMapper.toUpdateCommand(clientId, newClientDTO);
        Client updated = updateClientUseCase.execute(command);
        return ResponseEntity.ok(clientRestMapper.toDto(updated));
    }

    @Override
    public ResponseEntity<Void> deleteClient(Long clientId) {
        deleteClientUseCase.execute(clientId);
        return ResponseEntity.noContent().build();
    }
}
