package com.billMate.billing.infrastructure.persistence;

import com.billMate.billing.domain.model.Client;
import com.billMate.billing.domain.port.out.ClientRepositoryPort;
import com.billMate.billing.entity.ClientEntity;
import com.billMate.billing.infrastructure.persistence.repository.SpringDataClientRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ClientJpaAdapter implements ClientRepositoryPort {

    private final SpringDataClientRepository springDataClientRepository;

    public ClientJpaAdapter(SpringDataClientRepository springDataClientRepository) {
        this.springDataClientRepository = springDataClientRepository;
    }

    @Override
    public Client save(Client client) {
        ClientEntity entity = toEntity(client);
        ClientEntity saved = springDataClientRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Client> findById(Long id) {
        return springDataClientRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Client> findAll() {
        return springDataClientRepository.findAll()
                .stream()
                .map(this::toDomain)
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

    private ClientEntity toEntity(Client client) {
        return ClientEntity.builder()
                .id(client.getId())
                .name(client.getName())
                .email(client.getEmail())
                .phone(client.getPhone())
                .nif(client.getNif())
                .address(client.getAddress())
                .createdAt(client.getCreatedAt())
                .build();
    }

    private Client toDomain(ClientEntity entity) {
        return new Client(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getPhone(),
                entity.getNif(),
                entity.getAddress(),
                entity.getCreatedAt()
        );
    }
}
