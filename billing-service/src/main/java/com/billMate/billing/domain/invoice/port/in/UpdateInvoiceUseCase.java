package com.billMate.billing.domain.invoice.port.in;

import com.billMate.billing.domain.invoice.model.Invoice;

public interface UpdateInvoiceUseCase {

    Invoice execute(UpdateInvoiceCommand command);
}
