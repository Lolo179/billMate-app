package com.billMate.billing.application.service;


import com.billMate.billing.infrastructure.rest.dto.InvoiceDTO;
import com.billMate.billing.infrastructure.rest.dto.NewInvoiceDTO;

import java.util.List;

public interface InvoiceService {

    List<InvoiceDTO> getAllInvoices();

    InvoiceDTO getInvoiceById(Long invoiceId);

    InvoiceDTO createInvoice(NewInvoiceDTO newInvoiceDTO);

    InvoiceDTO updateInvoice(Long invoiceId, NewInvoiceDTO newInvoiceDTO);

    void deleteInvoice(Long invoiceId);

    List<InvoiceDTO> getInvoicesByClientId(Long clientId);

    byte[] emitInvoice(Long invoiceId);

    byte[] downloadInvoicePdf(Long invoiceId);

    InvoiceDTO markAsPaid(Long invoiceId);


}
