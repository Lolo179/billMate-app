package com.billMate.billing.infrastructure.rest.mapper;

import com.billMate.billing.domain.client.model.Client;
import com.billMate.billing.domain.client.port.in.CreateClientCommand;
import com.billMate.billing.domain.client.port.in.UpdateClientCommand;
import com.billMate.billing.domain.client.port.in.command.PatchClientCommand;
import com.billMate.billing.domain.client.port.in.query.ClientSearchQuery;
import com.billMate.billing.infrastructure.rest.dto.ClientDTO;
import com.billMate.billing.infrastructure.rest.dto.NewClientDTO;
import com.billMate.billing.infrastructure.rest.dto.PatchClientDTO;
import org.springframework.stereotype.Component;

import java.util.Optional;

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

    /**
     * Convierte los parámetros de query REST a un ClientSearchQuery de dominio.
     * El parseo del campo sort se delega al servicio de aplicación (validación whitelist).
     */
    public ClientSearchQuery toSearchQuery(Integer page, Integer size, String sort,
                                          String name, String nif) {
        String[] sortParts = parseSortParam(sort, "createdAt,desc");
        return new ClientSearchQuery(
                page != null ? page : 0,
                size != null ? size : 20,
                sortParts[0],
                sortParts[1],
                name,
                nif
        );
    }

    /**
     * Construye un PatchClientCommand mapeando cada campo a Optional:
     * - null en el DTO → Optional.empty() → campo NO se actualiza
     * - valor presente → Optional.of(valor) → campo se actualiza
     */
    public PatchClientCommand toPatchCommand(Long clientId, PatchClientDTO dto) {
        return new PatchClientCommand(
                clientId,
                Optional.ofNullable(dto.getName()),
                Optional.ofNullable(dto.getEmail()),
                Optional.ofNullable(dto.getPhone()),
                Optional.ofNullable(dto.getNif()),
                Optional.ofNullable(dto.getAddress())
        );
    }

    private String[] parseSortParam(String sort, String defaultValue) {
        String effective = (sort != null && !sort.isBlank()) ? sort : defaultValue;
        String[] parts = effective.split(",", 2);
        return new String[]{parts[0].trim(), parts.length > 1 ? parts[1].trim() : "desc"};
    }
}
