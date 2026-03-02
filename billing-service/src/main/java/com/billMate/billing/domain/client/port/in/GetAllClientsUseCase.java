package com.billMate.billing.domain.client.port.in;

import com.billMate.billing.domain.client.model.Client;

import java.util.List;

public interface GetAllClientsUseCase {

    List<Client> execute();
}
