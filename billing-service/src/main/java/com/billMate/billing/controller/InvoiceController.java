package com.billMate.billing.controller;

import com.billMate.billing.api.InvoicesApi;
import com.billMate.billing.model.InvoiceDTO;
import com.billMate.billing.model.NewInvoiceDTO;
import com.billMate.billing.service.InvoiceService;
import com.billMate.billing.service.InvoiceServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class InvoiceController implements InvoicesApi {

    private final InvoiceService invoiceService;

    @Override
    public ResponseEntity<InvoiceDTO> createInvoice(NewInvoiceDTO newInvoiceDTO) {
        InvoiceDTO created = invoiceService.createInvoice(newInvoiceDTO);
        return ResponseEntity.status(201).body(created);
    }

    @Override
    public ResponseEntity<Void> deleteInvoice(Long invoiceId) {
        invoiceService.deleteInvoice(invoiceId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<InvoiceDTO> getInvoiceById(Long invoiceId) {
        InvoiceDTO invoice = invoiceService.getInvoiceById(invoiceId);
        return ResponseEntity.ok(invoice);
    }

    @Override
    public ResponseEntity<List<InvoiceDTO>> getInvoices() {
        List<InvoiceDTO> invoices = invoiceService.getAllInvoices();
        return ResponseEntity.ok(invoices);
    }

    @Override
    public ResponseEntity<List<InvoiceDTO>> getInvoicesByClientId(Long clientId) {
        List<InvoiceDTO> invoices = invoiceService.getInvoicesByClientId(clientId);
        return ResponseEntity.ok(invoices);
    }

    @Override
    public ResponseEntity<InvoiceDTO> updateInvoice(Long invoiceId, NewInvoiceDTO newInvoiceDTO) {
        InvoiceDTO updated = invoiceService.updateInvoice(invoiceId, newInvoiceDTO);
        return ResponseEntity.ok(updated);
    }
}
