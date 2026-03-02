package com.billMate.billing.infrastructure.persistence.adapter;

import com.billMate.billing.domain.client.model.Client;
import com.billMate.billing.domain.client.port.out.ClientRepositoryPort;
import com.billMate.billing.infrastructure.persistence.entity.ClientEntity;
import com.billMate.billing.infrastructure.persistence.mapper.ClientPersistenceMapper;
import com.billMate.billing.infrastructure.persistence.repository.SpringDataClientRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ClientJpaAdapter implements ClientRepositoryPort {

    private final SpringDataClientRepository springDataClientRepository;
    private final ClientPersistenceMapper clientPersistenceMapper;

    public ClientJpaAdapter(SpringDataClientRepository springDataClientRepository,
                            ClientPersistenceMapper clientPersistenceMapper) {
        this.springDataClientRepository = springDataClientRepository;
        this.clientPersistenceMapper = clientPersistenceMapper;
    }

    @Override
    public Client save(Client client) {
        ClientEntity entity = clientPersistenceMapper.toEntity(client);
        ClientEntity saved = springDataClientRepository.save(entity);
        return clientPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<Client> findById(Long id) {
        return springDataClientRepository.findById(id).map(clientPersistenceMapper::toDomain);
    }

    @Override
    public List<Client> findAll() {
        return springDataClientRepository.findAll()
                .stream()
                .map(clientPersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        springDataClientRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return springDataClientRepository.existsById(id);
    }
}
