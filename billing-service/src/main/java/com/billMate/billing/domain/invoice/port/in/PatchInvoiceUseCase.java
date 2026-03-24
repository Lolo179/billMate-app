package com.billMate.billing.domain.invoice.port.in;

import com.billMate.billing.domain.invoice.model.Invoice;
import com.billMate.billing.domain.invoice.port.in.command.PatchInvoiceCommand;

public interface PatchInvoiceUseCase {

    Invoice execute(PatchInvoiceCommand command);
}
