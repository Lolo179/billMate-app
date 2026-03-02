package com.billMate.billing.infrastructure.persistence.mapper;

import com.billMate.billing.domain.client.model.Client;
import com.billMate.billing.infrastructure.persistence.entity.ClientEntity;
import org.springframework.stereotype.Component;

@Component
public class ClientPersistenceMapper {

    public ClientEntity toEntity(Client client) {
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

    public Client toDomain(ClientEntity entity) {
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
