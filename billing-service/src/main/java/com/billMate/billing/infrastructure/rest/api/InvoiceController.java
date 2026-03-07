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
import lombok.extern.slf4j.Slf4j;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
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
        log.info(">> POST /invoices", kv("clientId", newInvoiceDTO.getClientId()));
        CreateInvoiceCommand command = invoiceRestMapper.toCreateCommand(newInvoiceDTO);
        Invoice invoice = createInvoiceUseCase.execute(command);
        log.info("<< POST /invoices - Invoice created", kv("invoiceId", invoice.getId()));
        return ResponseEntity.status(201).body(invoiceRestMapper.toDto(invoice));
    }

    @Override
    public ResponseEntity<Void> deleteInvoice(Long invoiceId) {
        log.info(">> DELETE /invoices/{id}", kv("invoiceId", invoiceId));
        deleteInvoiceUseCase.execute(invoiceId);
        log.info("<< DELETE /invoices/{id}", kv("invoiceId", invoiceId));
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Resource> downloadInvoicePdf(Long invoiceId) {
        log.info(">> GET /invoices/{id}/pdf", kv("invoiceId", invoiceId));
        byte[] pdf = downloadInvoicePdfUseCase.execute(invoiceId);
        log.info("<< GET /invoices/{id}/pdf", kv("invoiceId", invoiceId), kv("bytes", pdf.length));
        return buildPdfResponse(pdf, invoiceId);
    }

    @Override
    public ResponseEntity<Resource> emitInvoice(Long invoiceId) {
        log.info(">> POST /invoices/{id}/emit", kv("invoiceId", invoiceId));
        byte[] pdf = emitInvoiceUseCase.execute(invoiceId);
        log.info("<< POST /invoices/{id}/emit", kv("invoiceId", invoiceId), kv("bytes", pdf.length));
        return buildPdfResponse(pdf, invoiceId);
    }

    @Override
    public ResponseEntity<InvoiceDTO> getInvoiceById(Long invoiceId) {
        log.info(">> GET /invoices/{id}", kv("invoiceId", invoiceId));
        Invoice invoice = getInvoiceUseCase.execute(invoiceId);
        log.info("<< GET /invoices/{id}", kv("invoiceId", invoiceId));
        return ResponseEntity.ok(invoiceRestMapper.toDto(invoice));
    }

    @Override
    public ResponseEntity<List<InvoiceDTO>> getInvoices() {
        log.info(">> GET /invoices");
        List<InvoiceDTO> invoices = getAllInvoicesUseCase.execute().stream()
                .map(invoiceRestMapper::toDto)
                .collect(Collectors.toList());
        log.info("<< GET /invoices", kv("count", invoices.size()));
        return ResponseEntity.ok(invoices);
    }

    @Override
    public ResponseEntity<List<InvoiceDTO>> getInvoicesByClientId(Long clientId) {
        log.info(">> GET /invoices?clientId", kv("clientId", clientId));
        List<InvoiceDTO> invoices = getInvoicesByClientUseCase.execute(clientId).stream()
                .map(invoiceRestMapper::toDto)
                .collect(Collectors.toList());
        log.info("<< GET /invoices by client", kv("clientId", clientId), kv("count", invoices.size()));
        return ResponseEntity.ok(invoices);
    }

    @Override
    public ResponseEntity<InvoiceDTO> payInvoice(Long invoiceId) {
        log.info(">> POST /invoices/{id}/pay", kv("invoiceId", invoiceId));
        Invoice invoice = payInvoiceUseCase.execute(invoiceId);
        log.info("<< POST /invoices/{id}/pay", kv("invoiceId", invoiceId));
        return ResponseEntity.ok(invoiceRestMapper.toDto(invoice));
    }

    @Override
    public ResponseEntity<InvoiceDTO> updateInvoice(Long invoiceId, NewInvoiceDTO newInvoiceDTO) {
        log.info(">> PUT /invoices/{id}", kv("invoiceId", invoiceId));
        UpdateInvoiceCommand command = invoiceRestMapper.toUpdateCommand(invoiceId, newInvoiceDTO);
        Invoice invoice = updateInvoiceUseCase.execute(command);
        log.info("<< PUT /invoices/{id}", kv("invoiceId", invoiceId));
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
