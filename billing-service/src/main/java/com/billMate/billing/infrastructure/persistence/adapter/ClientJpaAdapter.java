package com.billMate.billing.infrastructure.persistence.adapter;

import com.billMate.billing.domain.client.model.Client;
import com.billMate.billing.domain.client.port.out.ClientRepositoryPort;
import com.billMate.billing.domain.shared.PageResult;
import com.billMate.billing.infrastructure.persistence.entity.ClientEntity;
import com.billMate.billing.infrastructure.persistence.mapper.ClientPersistenceMapper;
import com.billMate.billing.infrastructure.persistence.repository.SpringDataClientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Component
public class ClientJpaAdapter implements ClientRepositoryPort {

    private static final Logger log = LoggerFactory.getLogger(ClientJpaAdapter.class);
    private final SpringDataClientRepository springDataClientRepository;
    private final ClientPersistenceMapper clientPersistenceMapper;

    public ClientJpaAdapter(SpringDataClientRepository springDataClientRepository,
                            ClientPersistenceMapper clientPersistenceMapper) {
        this.springDataClientRepository = springDataClientRepository;
        this.clientPersistenceMapper = clientPersistenceMapper;
    }

    @Override
    public Client save(Client client) {
        log.debug("Persisting client", kv("nif", client.getNif()));
        ClientEntity entity = clientPersistenceMapper.toEntity(client);
        ClientEntity saved = springDataClientRepository.save(entity);
        log.debug("Client persisted", kv("clientId", saved.getId()));
        return clientPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<Client> findById(Long id) {
        log.debug("Querying client in DB", kv("clientId", id));
        return springDataClientRepository.findById(id).map(clientPersistenceMapper::toDomain);
    }

    @Override
    public PageResult<Client> findAll(int page, int size) {
        log.debug("Querying clients in DB", kv("page", page), kv("size", size));
        Page<ClientEntity> clientsPage = springDataClientRepository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt", "id"))
        );

        return new PageResult<>(
                clientsPage.getContent().stream().map(clientPersistenceMapper::toDomain).toList(),
                clientsPage.getNumber(),
                clientsPage.getSize(),
                clientsPage.getTotalElements(),
                clientsPage.getTotalPages()
        );
    }

    @Override
    public void deleteById(Long id) {
        log.debug("Deleting client from DB", kv("clientId", id));
        springDataClientRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        log.debug("Checking client existence", kv("clientId", id));
        return springDataClientRepository.existsById(id);
    }
}
