package com.billMate.billing.infrastructure.rest.mapper;

import com.billMate.billing.domain.client.model.Client;
import com.billMate.billing.domain.client.port.in.CreateClientCommand;
import com.billMate.billing.domain.client.port.in.UpdateClientCommand;
import com.billMate.billing.infrastructure.rest.dto.ClientDTO;
import com.billMate.billing.infrastructure.rest.dto.NewClientDTO;
import org.springframework.stereotype.Component;

@Component
public class ClientRestMapper {

    public ClientDTO toDto(Client client) {
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

    public CreateClientCommand toCreateCommand(NewClientDTO dto) {
        return new CreateClientCommand(
                dto.getName(),
                dto.getEmail(),
                dto.getPhone(),
                dto.getNif(),
                dto.getAddress()
        );
    }

    public UpdateClientCommand toUpdateCommand(Long clientId, NewClientDTO dto) {
        return new UpdateClientCommand(
                clientId,
                dto.getName(),
                dto.getEmail(),
                dto.getPhone(),
                dto.getNif(),
                dto.getAddress()
        );
    }
}
