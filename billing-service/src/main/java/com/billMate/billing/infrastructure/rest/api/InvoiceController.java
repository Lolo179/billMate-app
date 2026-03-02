package com.billMate.billing.infrastructure.rest.api;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.billMate.billing.domain.invoice.model.Invoice;
import com.billMate.billing.domain.invoice.port.in.*;
import com.billMate.billing.infrastructure.rest.dto.InvoiceDTO;
import com.billMate.billing.infrastructure.rest.dto.NewInvoiceDTO;
import com.billMate.billing.infrastructure.rest.mapper.InvoiceRestMapper;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class InvoiceController implements InvoicesApi {

    private final CreateInvoiceUseCase createInvoiceUseCase;
    private final GetInvoiceUseCase getInvoiceUseCase;
    private final GetAllInvoicesUseCase getAllInvoicesUseCase;
    private final GetInvoicesByClientUseCase getInvoicesByClientUseCase;
    private final UpdateInvoiceUseCase updateInvoiceUseCase;
    private final DeleteInvoiceUseCase deleteInvoiceUseCase;
    private final EmitInvoiceUseCase emitInvoiceUseCase;
    private final DownloadInvoicePdfUseCase downloadInvoicePdfUseCase;
    private final PayInvoiceUseCase payInvoiceUseCase;
    private final InvoiceRestMapper invoiceRestMapper;

    @Override
    public ResponseEntity<InvoiceDTO> createInvoice(NewInvoiceDTO newInvoiceDTO) {
        CreateInvoiceCommand command = invoiceRestMapper.toCreateCommand(newInvoiceDTO);
        Invoice invoice = createInvoiceUseCase.execute(command);
        return ResponseEntity.status(201).body(invoiceRestMapper.toDto(invoice));
    }

    @Override
    public ResponseEntity<Void> deleteInvoice(Long invoiceId) {
        deleteInvoiceUseCase.execute(invoiceId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Resource> downloadInvoicePdf(Long invoiceId) {
        byte[] pdf = downloadInvoicePdfUseCase.execute(invoiceId);
        return buildPdfResponse(pdf, invoiceId);
    }

    @Override
    public ResponseEntity<Resource> emitInvoice(Long invoiceId) {
        byte[] pdf = emitInvoiceUseCase.execute(invoiceId);
        return buildPdfResponse(pdf, invoiceId);
    }

    @Override
    public ResponseEntity<InvoiceDTO> getInvoiceById(Long invoiceId) {
        Invoice invoice = getInvoiceUseCase.execute(invoiceId);
        return ResponseEntity.ok(invoiceRestMapper.toDto(invoice));
    }

    @Override
    public ResponseEntity<List<InvoiceDTO>> getInvoices() {
        List<InvoiceDTO> invoices = getAllInvoicesUseCase.execute().stream()
                .map(invoiceRestMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(invoices);
    }

    @Override
    public ResponseEntity<List<InvoiceDTO>> getInvoicesByClientId(Long clientId) {
        List<InvoiceDTO> invoices = getInvoicesByClientUseCase.execute(clientId).stream()
                .map(invoiceRestMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(invoices);
    }

    @Override
    public ResponseEntity<InvoiceDTO> payInvoice(Long invoiceId) {
        Invoice invoice = payInvoiceUseCase.execute(invoiceId);
        return ResponseEntity.ok(invoiceRestMapper.toDto(invoice));
    }

    @Override
    public ResponseEntity<InvoiceDTO> updateInvoice(Long invoiceId, NewInvoiceDTO newInvoiceDTO) {
        UpdateInvoiceCommand command = invoiceRestMapper.toUpdateCommand(invoiceId, newInvoiceDTO);
        Invoice invoice = updateInvoiceUseCase.execute(command);
        return ResponseEntity.ok(invoiceRestMapper.toDto(invoice));
    }

    private ResponseEntity<Resource> buildPdfResponse(byte[] pdf, Long invoiceId) {
        ByteArrayResource resource = new ByteArrayResource(pdf);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("factura_" + invoiceId + ".pdf")
                .build());
        return ResponseEntity.ok().headers(headers).body(resource);
    }
}
