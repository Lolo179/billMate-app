package com.billMate.billing.controller;

import com.billMate.billing.api.ClientsApi;
import com.billMate.billing.model.Client;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
public class ClientController implements ClientsApi {

    @Override
    public ResponseEntity<List<Client>> getClients() {
        // 🧪 Solo mock inicial
        return ResponseEntity.ok(List.of());
    }

    @Override
    public ResponseEntity<Client> createClient(Client client) {
        // 🧪 Devuelve el mismo cliente como si se hubiera guardado
        return ResponseEntity.status(201).body(client);
    }

    @Override
    public ResponseEntity<Client> getClientById(Long id) {
        // 🧪 Devuelve cliente dummy
        Client dummy = new Client();
        dummy.setId(id);
        dummy.setName("Cliente Test");
        dummy.setEmail("cliente@mail.com");
        dummy.setPhone("+34 600 123 123");
        dummy.setNif("12345678Z");
        dummy.setAddress("Calle Falsa 123");
        dummy.setCreatedAt(OffsetDateTime.parse("2025-05-15T00:00:00Z"));

        return ResponseEntity.ok(dummy);
    }

    @Override
    public ResponseEntity<Client> updateClient(Long id, Client client) {
        // 🧪 Simula actualización
        client.setId(id);
        return ResponseEntity.ok(client);
    }

    @Override
    public ResponseEntity<Void> deleteClient(Long id) {
        // 🧪 Simula borrado
        return ResponseEntity.noContent().build();
    }

}
