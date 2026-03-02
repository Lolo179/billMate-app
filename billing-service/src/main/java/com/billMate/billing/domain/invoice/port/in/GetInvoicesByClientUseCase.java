package com.billMate.billing.domain.invoice.port.in;

import com.billMate.billing.domain.invoice.model.Invoice;

import java.util.List;

public interface GetInvoicesByClientUseCase {

    List<Invoice> execute(Long clientId);
}
