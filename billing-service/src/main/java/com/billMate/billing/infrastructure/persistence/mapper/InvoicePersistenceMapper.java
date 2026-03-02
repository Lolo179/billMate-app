package com.billMate.billing.infrastructure.persistence.mapper;

import com.billMate.billing.domain.invoice.model.Invoice;
import com.billMate.billing.domain.invoice.model.InvoiceLineItem;
import com.billMate.billing.infrastructure.persistence.entity.InvoiceEntity;
import com.billMate.billing.infrastructure.persistence.entity.InvoiceLineEntity;
import com.billMate.billing.infrastructure.persistence.repository.SpringDataClientRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class InvoicePersistenceMapper {

    private final SpringDataClientRepository springDataClientRepository;

    public InvoicePersistenceMapper(SpringDataClientRepository springDataClientRepository) {
        this.springDataClientRepository = springDataClientRepository;
    }

    public InvoiceEntity toNewEntity(Invoice invoice) {
        List<InvoiceLineEntity> lineEntities = invoice.getLines().stream()
                .map(line -> InvoiceLineEntity.builder()
                        .description(line.getDescription())
                        .quantity(line.getQuantity())
                        .unitPrice(line.getUnitPrice())
                        .total(line.getTotal())
                        .build())
                .collect(Collectors.toList());

        return InvoiceEntity.builder()
                .client(springDataClientRepository.getReferenceById(invoice.getClientId()))
                .invoiceLines(lineEntities)
                .date(invoice.getDate())
                .status(invoice.getStatus())
                .description(invoice.getDescription())
                .total(invoice.getTotal())
                .taxPercentage(invoice.getTaxPercentage())
                .createdAt(invoice.getCreatedAt())
                .build();
    }

    public Invoice toDomain(InvoiceEntity entity) {
        List<InvoiceLineItem> lines = entity.getInvoiceLines().stream()
                .map(line -> new InvoiceLineItem(
                        line.getId(),
                        line.getDescription(),
                        line.getQuantity(),
                        line.getUnitPrice(),
                        line.getTotal()))
                .collect(Collectors.toList());

        return new Invoice(
                entity.getInvoiceId(),
                entity.getClient().getId(),
                lines,
                entity.getDate(),
                entity.getStatus(),
                entity.getDescription(),
                entity.getTotal(),
                entity.getTaxPercentage(),
                entity.getCreatedAt()
        );
    }
}
