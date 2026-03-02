package com.billMate.billing.domain.client.port.in;

import com.billMate.billing.domain.client.model.Client;

public interface UpdateClientUseCase {

    Client execute(UpdateClientCommand command);
}
