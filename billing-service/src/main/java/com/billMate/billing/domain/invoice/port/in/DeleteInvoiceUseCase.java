package com.billMate.billing.domain.invoice.port.in;

public interface DeleteInvoiceUseCase {

    void execute(Long invoiceId);
}
