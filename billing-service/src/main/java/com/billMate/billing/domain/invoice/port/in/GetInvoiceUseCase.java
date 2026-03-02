package com.billMate.billing.domain.invoice.port.in;

import com.billMate.billing.domain.invoice.model.Invoice;

public interface GetInvoiceUseCase {

    Invoice execute(Long invoiceId);
}
