package com.billMate.billing.infrastructure.rest.api;

import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.billMate.billing.infrastructure.rest.api.InvoicesApi;
import com.billMate.billing.domain.invoice.model.Invoice;
import com.billMate.billing.domain.invoice.model.InvoiceLineItem;
import com.billMate.billing.domain.invoice.port.in.*;
import com.billMate.billing.infrastructure.rest.dto.InvoiceDTO;
import com.billMate.billing.infrastructure.rest.dto.InvoiceLine;
import com.billMate.billing.infrastructure.rest.dto.NewInvoiceDTO;

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

    @Override
    public ResponseEntity<InvoiceDTO> createInvoice(NewInvoiceDTO newInvoiceDTO) {
        CreateInvoiceCommand command = toCreateCommand(newInvoiceDTO);
        Invoice invoice = createInvoiceUseCase.execute(command);
        return ResponseEntity.status(201).body(toDto(invoice));
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
        return ResponseEntity.ok(toDto(invoice));
    }

    @Override
    public ResponseEntity<List<InvoiceDTO>> getInvoices() {
        List<InvoiceDTO> invoices = getAllInvoicesUseCase.execute().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(invoices);
    }

    @Override
    public ResponseEntity<List<InvoiceDTO>> getInvoicesByClientId(Long clientId) {
        List<InvoiceDTO> invoices = getInvoicesByClientUseCase.execute(clientId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(invoices);
    }

    @Override
    public ResponseEntity<InvoiceDTO> payInvoice(Long invoiceId) {
        Invoice invoice = payInvoiceUseCase.execute(invoiceId);
        return ResponseEntity.ok(toDto(invoice));
    }

    @Override
    public ResponseEntity<InvoiceDTO> updateInvoice(Long invoiceId, NewInvoiceDTO newInvoiceDTO) {
        UpdateInvoiceCommand command = toUpdateCommand(invoiceId, newInvoiceDTO);
        Invoice invoice = updateInvoiceUseCase.execute(command);
        return ResponseEntity.ok(toDto(invoice));
    }

    // ---- Mappers ----

    private InvoiceDTO toDto(Invoice invoice) {
        InvoiceDTO dto = new InvoiceDTO();
        dto.setInvoiceId(invoice.getId());
        dto.setClientId(invoice.getClientId());
        dto.setDate(invoice.getDate());
        dto.setDescription(invoice.getDescription());
        dto.setTotal(invoice.getTotal());
        dto.setTaxPercentage(invoice.getTaxPercentage() != null
                ? invoice.getTaxPercentage().doubleValue() : null);
        dto.setStatus(invoice.getStatus() != null
                ? InvoiceDTO.StatusEnum.valueOf(invoice.getStatus().name()) : null);
        dto.setCreatedAt(invoice.getCreatedAt() != null
                ? invoice.getCreatedAt().atOffset(ZoneOffset.UTC) : null);
        dto.setInvoiceLines(invoice.getLines().stream()
                .map(this::toLineDto)
                .collect(Collectors.toList()));
        return dto;
    }

    private InvoiceLine toLineDto(InvoiceLineItem line) {
        InvoiceLine dto = new InvoiceLine();
        dto.setDescription(line.getDescription());
        dto.setQuantity(line.getQuantity() != null ? line.getQuantity().doubleValue() : null);
        dto.setUnitPrice(line.getUnitPrice() != null ? line.getUnitPrice().doubleValue() : null);
        return dto;
    }

    private CreateInvoiceCommand toCreateCommand(NewInvoiceDTO dto) {
        List<CreateInvoiceCommand.LineCommand> lines = dto.getInvoiceLines().stream()
                .map(l -> new CreateInvoiceCommand.LineCommand(
                        l.getDescription(), l.getQuantity(), l.getUnitPrice()))
                .collect(Collectors.toList());
        return new CreateInvoiceCommand(
                dto.getClientId(),
                dto.getDate(),
                dto.getDescription(),
                dto.getStatus() != null ? dto.getStatus().name() : null,
                lines
        );
    }

    private UpdateInvoiceCommand toUpdateCommand(Long invoiceId, NewInvoiceDTO dto) {
        List<UpdateInvoiceCommand.LineCommand> lines = dto.getInvoiceLines().stream()
                .map(l -> new UpdateInvoiceCommand.LineCommand(
                        l.getDescription(), l.getQuantity(), l.getUnitPrice()))
                .collect(Collectors.toList());
        return new UpdateInvoiceCommand(
                invoiceId,
                dto.getClientId(),
                dto.getDate(),
                dto.getDescription(),
                dto.getStatus() != null ? dto.getStatus().name() : null,
                lines
        );
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
