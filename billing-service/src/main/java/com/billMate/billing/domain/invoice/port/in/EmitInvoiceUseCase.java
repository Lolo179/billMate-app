package com.billMate.billing.domain.invoice.port.in;

public interface EmitInvoiceUseCase {

    byte[] execute(Long invoiceId);
}
