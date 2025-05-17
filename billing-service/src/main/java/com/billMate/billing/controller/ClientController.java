package com.billMate.billing.controller;

import com.billMate.billing.api.ClientsApi;
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

    private final ClientService clientService;

    @Override
    public ResponseEntity<ClientDTO> createClient(NewClientDTO newClientDTO) {
        ClientDTO saved = clientService.createClient(newClientDTO);
        return ResponseEntity.status(201).body(saved);
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
}
