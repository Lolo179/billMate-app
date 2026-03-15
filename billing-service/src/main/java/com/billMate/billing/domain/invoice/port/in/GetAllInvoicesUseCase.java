package com.billMate.billing.domain.invoice.port.in;

import com.billMate.billing.domain.invoice.model.Invoice;
import com.billMate.billing.domain.shared.PageResult;

public interface GetAllInvoicesUseCase {

    PageResult<Invoice> execute(int page, int size);
}
