package com.billMate.billing.infrastructure.rest.mapper;

import com.billMate.billing.domain.invoice.model.Invoice;
import com.billMate.billing.domain.invoice.model.InvoiceLineItem;
import com.billMate.billing.domain.invoice.model.InvoiceStatus;
import com.billMate.billing.domain.invoice.port.in.CreateInvoiceCommand;
import com.billMate.billing.domain.invoice.port.in.UpdateInvoiceCommand;
import com.billMate.billing.domain.invoice.port.in.command.PatchInvoiceCommand;
import com.billMate.billing.domain.invoice.port.in.query.InvoiceSearchQuery;
import com.billMate.billing.infrastructure.rest.dto.InvoiceDTO;
import com.billMate.billing.infrastructure.rest.dto.InvoiceLine;
import com.billMate.billing.infrastructure.rest.dto.NewInvoiceDTO;
import com.billMate.billing.infrastructure.rest.dto.PatchInvoiceDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class InvoiceRestMapper {

    public InvoiceDTO toDto(Invoice invoice) {
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

    public InvoiceLine toLineDto(InvoiceLineItem line) {
        InvoiceLine dto = new InvoiceLine();
        dto.setDescription(line.getDescription());
        dto.setQuantity(line.getQuantity() != null ? line.getQuantity().doubleValue() : null);
        dto.setUnitPrice(line.getUnitPrice() != null ? line.getUnitPrice().doubleValue() : null);
        dto.setTotal(line.getTotal() != null ? line.getTotal().doubleValue() : null);
        return dto;
    }

    public CreateInvoiceCommand toCreateCommand(NewInvoiceDTO dto) {
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

    public UpdateInvoiceCommand toUpdateCommand(Long invoiceId, NewInvoiceDTO dto) {
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

    /**
     * Convierte los parámetros de query REST a un InvoiceSearchQuery de dominio.
     * Si status es inválido lanza IllegalArgumentException (→ 400 en GlobalExceptionHandler).
     */
    public InvoiceSearchQuery toSearchQuery(Integer page, Integer size, String sort,
                                           String statusStr, LocalDate dateFrom,
                                           LocalDate dateTo, Long clientId) {
        String[] sortParts = parseSortParam(sort, "createdAt,desc");
        InvoiceStatus status = parseStatus(statusStr);
        return new InvoiceSearchQuery(
                page != null ? page : 0,
                size != null ? size : 20,
                sortParts[0],
                sortParts[1],
                status,
                dateFrom,
                dateTo,
                clientId
        );
    }

    /**
     * Construye un PatchInvoiceCommand mapeando cada campo a Optional:
     * - null en el DTO → Optional.empty() → campo NO se actualiza
     * - valor presente → Optional.of(valor) → campo se actualiza
     */
    public PatchInvoiceCommand toPatchCommand(Long invoiceId, PatchInvoiceDTO dto) {
        Optional<List<PatchInvoiceCommand.LineCommand>> lines =
                Optional.ofNullable(dto.getInvoiceLines())
                        .map(l -> l.stream()
                                .map(line -> new PatchInvoiceCommand.LineCommand(
                                        line.getDescription(), line.getQuantity(), line.getUnitPrice()))
                                .toList());
        return new PatchInvoiceCommand(
                invoiceId,
                Optional.ofNullable(dto.getClientId()),
                Optional.ofNullable(dto.getDate()),
                Optional.ofNullable(dto.getDescription()),
                lines
        );
    }

    private InvoiceStatus parseStatus(String statusStr) {
        if (statusStr == null || statusStr.isBlank()) {
            return null;
        }
        try {
            return InvoiceStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Estado de factura inválido: '" + statusStr + "'. Valores permitidos: DRAFT, SENT, PAID, CANCELLED");
        }
    }

    private String[] parseSortParam(String sort, String defaultValue) {
        String effective = (sort != null && !sort.isBlank()) ? sort : defaultValue;
        String[] parts = effective.split(",", 2);
        return new String[]{parts[0].trim(), parts.length > 1 ? parts[1].trim() : "desc"};
    }
}


