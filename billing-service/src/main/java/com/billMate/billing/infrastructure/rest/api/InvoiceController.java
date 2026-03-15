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
import com.billMate.billing.domain.shared.PageResult;
import com.billMate.billing.infrastructure.rest.dto.InvoiceDTO;
import com.billMate.billing.infrastructure.rest.dto.InvoicePageDTO;
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
        log.info(">> PUT /invoices/{id}/emit", kv("invoiceId", invoiceId));
        byte[] pdf = emitInvoiceUseCase.execute(invoiceId);
        log.info("<< PUT /invoices/{id}/emit", kv("invoiceId", invoiceId), kv("bytes", pdf.length));
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
    public ResponseEntity<InvoicePageDTO> getInvoices(Integer page, Integer size) {
        int requestedPage = page != null ? page : 0;
        int requestedSize = size != null ? size : 20;

        log.info(">> GET /invoices", kv("page", requestedPage), kv("size", requestedSize));
        PageResult<Invoice> invoices = getAllInvoicesUseCase.execute(requestedPage, requestedSize);

        InvoicePageDTO response = new InvoicePageDTO();
        response.setItems(invoices.items().stream().map(invoiceRestMapper::toDto).collect(Collectors.toList()));
        response.setPage(invoices.page());
        response.setSize(invoices.size());
        response.setTotalElements(invoices.totalElements());
        response.setTotalPages(invoices.totalPages());

        log.info("<< GET /invoices", kv("count", response.getItems().size()), kv("totalElements", response.getTotalElements()));
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<InvoicePageDTO> getInvoicesByClientId(Long clientId, Integer page, Integer size) {
        int requestedPage = page != null ? page : 0;
        int requestedSize = size != null ? size : 20;

        log.info(">> GET /invoices/client/{clientId}", kv("clientId", clientId), kv("page", requestedPage), kv("size", requestedSize));
        PageResult<Invoice> invoices = getInvoicesByClientUseCase.execute(clientId, requestedPage, requestedSize);

        InvoicePageDTO response = new InvoicePageDTO();
        response.setItems(invoices.items().stream().map(invoiceRestMapper::toDto).collect(Collectors.toList()));
        response.setPage(invoices.page());
        response.setSize(invoices.size());
        response.setTotalElements(invoices.totalElements());
        response.setTotalPages(invoices.totalPages());

        log.info("<< GET /invoices by client", kv("clientId", clientId), kv("count", response.getItems().size()), kv("totalElements", response.getTotalElements()));
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<InvoiceDTO> payInvoice(Long invoiceId) {
        log.info(">> PUT /invoices/{id}/pay", kv("invoiceId", invoiceId));
        Invoice invoice = payInvoiceUseCase.execute(invoiceId);
        log.info("<< PUT /invoices/{id}/pay", kv("invoiceId", invoiceId));
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
