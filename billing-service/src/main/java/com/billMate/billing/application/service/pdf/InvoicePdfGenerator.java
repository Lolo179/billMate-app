package com.billMate.billing.application.service.pdf;

import com.billMate.billing.infrastructure.persistence.entity.InvoiceEntity;
import com.billMate.billing.infrastructure.rest.dto.InvoiceDTO;

public interface InvoicePdfGenerator {
    byte[] generate(InvoiceEntity invoice);
    byte[] downloadInvoicePdf(Long invoiceId);

}
