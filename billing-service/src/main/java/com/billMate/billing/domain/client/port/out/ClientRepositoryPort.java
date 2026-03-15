package com.billMate.billing.domain.client.port.out;

import com.billMate.billing.domain.client.model.Client;
import com.billMate.billing.domain.shared.PageResult;

import java.util.Optional;

public interface ClientRepositoryPort {

    Client save(Client client);

    Optional<Client> findById(Long id);

    PageResult<Client> findAll(int page, int size);

    void deleteById(Long id);

    boolean existsById(Long id);
}
