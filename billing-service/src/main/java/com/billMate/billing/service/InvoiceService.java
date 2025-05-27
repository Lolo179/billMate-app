package com.billMate.billing.service;


import com.billMate.billing.model.InvoiceDTO;
import com.billMate.billing.model.NewInvoiceDTO;

import java.util.List;

public interface InvoiceService {

    List<InvoiceDTO> getAllInvoices();

    InvoiceDTO getInvoiceById(Long invoiceId);

    InvoiceDTO createInvoice(NewInvoiceDTO newInvoiceDTO);

    InvoiceDTO updateInvoice(Long invoiceId, NewInvoiceDTO newInvoiceDTO);

    void deleteInvoice(Long invoiceId);

    List<InvoiceDTO> getInvoicesByClientId(Long clientId);
}
