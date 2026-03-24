package com.billMate.billing.infrastructure.persistence.adapter;

import com.billMate.billing.domain.client.model.Client;
import com.billMate.billing.domain.client.port.in.query.ClientSearchQuery;
import com.billMate.billing.domain.client.port.out.ClientRepositoryPort;
import com.billMate.billing.domain.shared.PageResult;
import com.billMate.billing.infrastructure.persistence.entity.ClientEntity;
import com.billMate.billing.infrastructure.persistence.mapper.ClientPersistenceMapper;
import com.billMate.billing.infrastructure.persistence.repository.SpringDataClientRepository;
import com.billMate.billing.infrastructure.persistence.specification.ClientSpecifications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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
    public PageResult<Client> search(ClientSearchQuery query) {
        log.debug("Searching clients in DB",
                kv("page", query.page()), kv("size", query.size()),
                kv("sort", query.sortField() + "," + query.sortDir()),
                kv("name", query.name()), kv("nif", query.nif()));

        Specification<ClientEntity> spec = ClientSpecifications.build(query);
        Sort sort = buildSort(query.sortField(), query.sortDir());
        Page<ClientEntity> clientsPage = springDataClientRepository.findAll(
                spec, PageRequest.of(query.page(), query.size(), sort));

        log.debug("Clients found", kv("count", clientsPage.getNumberOfElements()),
                kv("totalElements", clientsPage.getTotalElements()));

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

    /**
     * Construye un Sort de Spring Data a partir de los campos del query.
     * El campo sortField ya viene validado desde la capa de aplicación.
     */
    private Sort buildSort(String sortField, String sortDir) {
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir)
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(direction, sortField);
    }
}
