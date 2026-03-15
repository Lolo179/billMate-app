package com.billMate.billing.infrastructure.rest.api;

import com.billMate.billing.domain.client.model.Client;
import com.billMate.billing.domain.client.port.in.*;
import com.billMate.billing.domain.shared.PageResult;
import com.billMate.billing.infrastructure.rest.dto.ClientDTO;
import com.billMate.billing.infrastructure.rest.dto.ClientPageDTO;
import com.billMate.billing.infrastructure.rest.dto.NewClientDTO;
import com.billMate.billing.infrastructure.rest.mapper.ClientRestMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ClientController implements ClientsApi {

    private final CreateClientUseCase createClientUseCase;
    private final GetClientUseCase getClientUseCase;
    private final GetAllClientsUseCase getAllClientsUseCase;
    private final UpdateClientUseCase updateClientUseCase;
    private final DeleteClientUseCase deleteClientUseCase;
    private final ClientRestMapper clientRestMapper;

    @Override
    public ResponseEntity<ClientDTO> createClient(NewClientDTO newClientDTO) {
        log.info(">> POST /clients", kv("nif", newClientDTO.getNif()));
        CreateClientCommand command = clientRestMapper.toCreateCommand(newClientDTO);
        Client client = createClientUseCase.execute(command);
        log.info("<< POST /clients - Client created", kv("clientId", client.getId()));
        return ResponseEntity.status(201).body(clientRestMapper.toDto(client));
    }

    @Override
    public ResponseEntity<ClientDTO> getClientById(Long clientId) {
        log.info(">> GET /clients/{id}", kv("clientId", clientId));
        Client client = getClientUseCase.execute(clientId);
        log.info("<< GET /clients/{id}", kv("clientId", clientId));
        return ResponseEntity.ok(clientRestMapper.toDto(client));
    }

    @Override
    public ResponseEntity<ClientPageDTO> getClients(Integer page, Integer size) {
        int requestedPage = page != null ? page : 0;
        int requestedSize = size != null ? size : 20;

        log.info(">> GET /clients", kv("page", requestedPage), kv("size", requestedSize));
        PageResult<Client> clients = getAllClientsUseCase.execute(requestedPage, requestedSize);

        ClientPageDTO response = new ClientPageDTO();
        response.setItems(clients.items().stream().map(clientRestMapper::toDto).collect(Collectors.toList()));
        response.setPage(clients.page());
        response.setSize(clients.size());
        response.setTotalElements(clients.totalElements());
        response.setTotalPages(clients.totalPages());

        log.info("<< GET /clients", kv("count", response.getItems().size()), kv("totalElements", response.getTotalElements()));
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ClientDTO> updateClient(Long clientId, NewClientDTO newClientDTO) {
        log.info(">> PUT /clients/{id}", kv("clientId", clientId));
        UpdateClientCommand command = clientRestMapper.toUpdateCommand(clientId, newClientDTO);
        Client updated = updateClientUseCase.execute(command);
        log.info("<< PUT /clients/{id}", kv("clientId", clientId));
        return ResponseEntity.ok(clientRestMapper.toDto(updated));
    }

    @Override
    public ResponseEntity<Void> deleteClient(Long clientId) {
        log.info(">> DELETE /clients/{id}", kv("clientId", clientId));
        deleteClientUseCase.execute(clientId);
        log.info("<< DELETE /clients/{id}", kv("clientId", clientId));
        return ResponseEntity.noContent().build();
    }
}
