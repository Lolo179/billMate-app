package com.billMate.billing.controller;

import com.billMate.billing.api.ClientsApi;
import com.billMate.billing.domain.model.Client;
import com.billMate.billing.domain.port.in.CreateClientCommand;
import com.billMate.billing.domain.port.in.CreateClientUseCase;
import com.billMate.billing.model.ClientDTO;
import com.billMate.billing.model.NewClientDTO;
import com.billMate.billing.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ClientController implements ClientsApi {

    private final CreateClientUseCase createClientUseCase;
    private final ClientService clientService;

    @Override
    public ResponseEntity<ClientDTO> createClient(NewClientDTO newClientDTO) {
        CreateClientCommand command = new CreateClientCommand(
                newClientDTO.getName(),
                newClientDTO.getEmail(),
                newClientDTO.getPhone(),
                newClientDTO.getNif(),
                newClientDTO.getAddress()
        );
        Client client = createClientUseCase.execute(command);
        return ResponseEntity.status(201).body(toDto(client));
    }

    @Override
    public ResponseEntity<ClientDTO> getClientById(Long clientId) {
        ClientDTO client = clientService.getClientById(clientId);
        return ResponseEntity.ok(client);
    }

    @Override
    public ResponseEntity<List<ClientDTO>> getClients() {
        List<ClientDTO> clients = clientService.getAllClients();
        return ResponseEntity.ok(clients);
    }

    @Override
    public ResponseEntity<ClientDTO> updateClient(Long clientId, NewClientDTO newClientDTO) {
        ClientDTO updated = clientService.updateClient(clientId, newClientDTO);
        return ResponseEntity.ok(updated);
    }

    @Override
    public ResponseEntity<Void> deleteClient(Long clientId) {
        clientService.deleteClient(clientId);
        return ResponseEntity.noContent().build();
    }

    private ClientDTO toDto(Client client) {
        ClientDTO dto = new ClientDTO();
        dto.setClientId(client.getId());
        dto.setName(client.getName());
        dto.setEmail(client.getEmail());
        dto.setPhone(client.getPhone());
        dto.setNif(client.getNif());
        dto.setAddress(client.getAddress());
        dto.setCreatedAt(client.getCreatedAt());
        return dto;
    }
}
