package com.billMate.billing.domain.client.port.in;

import com.billMate.billing.domain.client.model.Client;

public interface GetClientUseCase {

    Client execute(Long clientId);
}
