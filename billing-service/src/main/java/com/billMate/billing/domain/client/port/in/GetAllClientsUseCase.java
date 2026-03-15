package com.billMate.billing.domain.client.port.in;

import com.billMate.billing.domain.client.model.Client;
import com.billMate.billing.domain.shared.PageResult;

public interface GetAllClientsUseCase {

    PageResult<Client> execute(int page, int size);
}
