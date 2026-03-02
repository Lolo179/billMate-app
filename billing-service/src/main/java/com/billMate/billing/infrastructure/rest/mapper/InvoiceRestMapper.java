package com.billMate.billing.infrastructure.rest.mapper;

import com.billMate.billing.domain.invoice.model.Invoice;
import com.billMate.billing.domain.invoice.model.InvoiceLineItem;
import com.billMate.billing.domain.invoice.port.in.CreateInvoiceCommand;
import com.billMate.billing.domain.invoice.port.in.UpdateInvoiceCommand;
import com.billMate.billing.infrastructure.rest.dto.InvoiceDTO;
import com.billMate.billing.infrastructure.rest.dto.InvoiceLine;
import com.billMate.billing.infrastructure.rest.dto.NewInvoiceDTO;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.util.List;
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
}
