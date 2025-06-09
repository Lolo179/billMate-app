package com.billMate.billing.controller;

import com.billMate.billing.api.InvoicesApi;
import com.billMate.billing.model.InvoiceDTO;
import com.billMate.billing.model.NewInvoiceDTO;
import com.billMate.billing.service.InvoiceService;
import com.billMate.billing.service.InvoiceServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
    public ResponseEntity<Resource> downloadInvoicePdf(Long invoiceId) {
        byte[] pdf = invoiceService.downloadInvoicePdf(invoiceId);

        ByteArrayResource resource = new ByteArrayResource(pdf);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("factura_" + invoiceId + ".pdf")
                .build());

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(resource);
    }


    @Override
    public ResponseEntity<Resource> emitInvoice(Long invoiceId) {
        byte[] pdf = invoiceService.emitInvoice(invoiceId);

        ByteArrayResource resource = new ByteArrayResource(pdf);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("factura_" + invoiceId + ".pdf")
                .build());

        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
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
    public ResponseEntity<InvoiceDTO> payInvoice(Long invoiceId) {
        InvoiceDTO updated = invoiceService.markAsPaid(invoiceId);
        return ResponseEntity.ok(updated);
    }

    @Override
    public ResponseEntity<InvoiceDTO> updateInvoice(Long invoiceId, NewInvoiceDTO newInvoiceDTO) {
        InvoiceDTO updated = invoiceService.updateInvoice(invoiceId, newInvoiceDTO);
        return ResponseEntity.ok(updated);
    }
}
