package com.billMate.billing.application.service;

import com.billMate.billing.infrastructure.persistence.entity.ClientEntity;
import com.billMate.billing.infrastructure.rest.dto.ClientDTO;
import com.billMate.billing.infrastructure.rest.dto.NewClientDTO;
import com.billMate.billing.infrastructure.persistence.repository.ClientRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final ModelMapper modelMapper;

    @Override
    public ClientDTO createClient(NewClientDTO newClientDTO) {
        ClientEntity clientEntity = modelMapper.map(newClientDTO, ClientEntity.class);
        clientEntity.setCreatedAt(OffsetDateTime.now());
        ClientEntity saved = clientRepository.save(clientEntity);
        return modelMapper.map(saved, ClientDTO.class);
    }

    @Override
    public ClientDTO getClientById(Long clientId) {
        ClientEntity entity = clientRepository.findById(clientId)
                .orElseThrow(() -> new EntityNotFoundException("Client not found with ID: " + clientId));
        return modelMapper.map(entity, ClientDTO.class);
    }

    @Override
    public List<ClientDTO> getAllClients() {
        return clientRepository.findAll()
                .stream()
                .map(entity -> modelMapper.map(entity, ClientDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public ClientDTO updateClient(Long clientId, NewClientDTO newClientDTO) {
        ClientEntity existing = clientRepository.findById(clientId)
                .orElseThrow(() -> new EntityNotFoundException("Client not found with ID: " + clientId));

        modelMapper.map(newClientDTO, existing); // copia los campos actualizables
        existing.setId(clientId); // aseguramos que el ID no cambia
        ClientEntity updated = clientRepository.save(existing);

        return modelMapper.map(updated, ClientDTO.class);
    }

    @Override
    public void deleteClient(Long clientId) {
        if (!clientRepository.existsById(clientId)) {
            throw new EntityNotFoundException("Client not found with ID: " + clientId);
        }
        clientRepository.deleteById(clientId);
    }
}
