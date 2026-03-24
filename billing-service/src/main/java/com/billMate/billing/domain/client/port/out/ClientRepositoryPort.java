package com.billMate.billing.domain.client.port.out;

import com.billMate.billing.domain.client.model.Client;
import com.billMate.billing.domain.client.port.in.query.ClientSearchQuery;
import com.billMate.billing.domain.shared.PageResult;

import java.util.Optional;

public interface ClientRepositoryPort {

    Client save(Client client);

    Optional<Client> findById(Long id);

    PageResult<Client> search(ClientSearchQuery query);

    void deleteById(Long id);

    boolean existsById(Long id);
}
