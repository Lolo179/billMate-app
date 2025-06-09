package com.billMate.billing.service.pdf;

import com.billMate.billing.entity.InvoiceEntity;
import com.billMate.billing.model.InvoiceDTO;

public interface InvoicePdfGenerator {
    byte[] generate(InvoiceEntity invoice);
    byte[] downloadInvoicePdf(Long invoiceId);

}
