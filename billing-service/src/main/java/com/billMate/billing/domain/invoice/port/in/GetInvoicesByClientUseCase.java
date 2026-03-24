package com.billMate.billing.domain.invoice.port.in;

import com.billMate.billing.domain.invoice.model.Invoice;
import com.billMate.billing.domain.invoice.port.in.query.InvoiceSearchQuery;
import com.billMate.billing.domain.shared.PageResult;

public interface GetInvoicesByClientUseCase {

    PageResult<Invoice> execute(InvoiceSearchQuery query);
}
