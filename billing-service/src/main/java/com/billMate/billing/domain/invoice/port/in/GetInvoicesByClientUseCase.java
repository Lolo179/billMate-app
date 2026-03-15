package com.billMate.billing.domain.invoice.port.in;

import com.billMate.billing.domain.invoice.model.Invoice;
import com.billMate.billing.domain.shared.PageResult;

public interface GetInvoicesByClientUseCase {

    PageResult<Invoice> execute(Long clientId, int page, int size);
}
