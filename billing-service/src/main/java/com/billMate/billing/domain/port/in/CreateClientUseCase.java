package com.billMate.billing.domain.port.in;

import com.billMate.billing.domain.model.Client;

public interface CreateClientUseCase {

    Client execute(CreateClientCommand command);
}
