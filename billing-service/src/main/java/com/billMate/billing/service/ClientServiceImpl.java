package com.billMate.billing.service;

import com.billMate.billing.config.MapperConfig;
import com.billMate.billing.entity.ClientEntity;
import com.billMate.billing.model.Client;
import com.billMate.billing.model.ClientService;
import com.billMate.billing.repository.JpaRepository.ClientRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final ModelMapper modelMapper;

    @Override
    public Client createClient(Client client) {

        ClientEntity clientEntity = modelMapper.map(client, ClientEntity.class);
        clientEntity.setCreatedAt(OffsetDateTime.now());
        ClientEntity saved = clientRepository.save(clientEntity);

        return modelMapper.map(saved, Client.class);
    }

    @Override
    public Client getClientById(Long id) {
        ClientEntity client = clientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Client not found with ID: " + id));

        return modelMapper.map(client, Client.class);
    }

    @Override
    public List<Client> getAllClients() {
        List<ClientEntity> entities = clientRepository.findAll();
        List<Client> clients = new ArrayList<>();

        for (ClientEntity entity : entities) {
            Client client = modelMapper.map(entity, Client.class);
            clients.add(client);
        }

        return clients;
    }

    @Override
    public Client updateClient(Long id, Client client) {
        ClientEntity existing = clientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Client not found with ID: " + id));

        modelMapper.map(client, existing);
        existing.setId(id);
        ClientEntity updated = clientRepository.save(existing);

        return modelMapper.map(updated, Client.class);
    }

    @Override
    public void deleteClient(Long id) {
        if (!clientRepository.existsById(id)) {
            throw new EntityNotFoundException("Client not found with ID: " + id);
        }
        clientRepository.deleteById(id);
    }

}
