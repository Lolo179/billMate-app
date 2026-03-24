package com.billMate.billing.domain.client.port.in;

import com.billMate.billing.domain.client.model.Client;
import com.billMate.billing.domain.client.port.in.command.PatchClientCommand;

public interface PatchClientUseCase {

    Client execute(PatchClientCommand command);
}
