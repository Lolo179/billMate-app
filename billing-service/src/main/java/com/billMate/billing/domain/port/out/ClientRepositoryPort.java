package com.billMate.billing.domain.port.out;

import com.billMate.billing.domain.model.Client;

import java.util.List;
import java.util.Optional;

public interface ClientRepositoryPort {

    Client save(Client client);

    Optional<Client> findById(Long id);

    List<Client> findAll();

    void deleteById(Long id);

    boolean existsById(Long id);
}
