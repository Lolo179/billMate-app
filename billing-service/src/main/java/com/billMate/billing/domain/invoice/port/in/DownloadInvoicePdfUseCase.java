package com.billMate.billing.domain.invoice.port.in;

public interface DownloadInvoicePdfUseCase {

    byte[] execute(Long invoiceId);
}
