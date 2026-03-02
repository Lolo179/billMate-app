package com.billMate.billing.infrastructure.persistence.adapter;

import com.billMate.billing.domain.invoice.model.Invoice;
import com.billMate.billing.domain.invoice.model.InvoiceLineItem;
import com.billMate.billing.domain.invoice.port.out.InvoiceRepositoryPort;
import com.billMate.billing.infrastructure.persistence.entity.InvoiceEntity;
import com.billMate.billing.infrastructure.persistence.entity.InvoiceLineEntity;
import com.billMate.billing.infrastructure.persistence.mapper.InvoicePersistenceMapper;
import com.billMate.billing.infrastructure.persistence.repository.SpringDataClientRepository;
import com.billMate.billing.infrastructure.persistence.repository.SpringDataInvoiceRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class InvoiceJpaAdapter implements InvoiceRepositoryPort {

    private final SpringDataInvoiceRepository springDataInvoiceRepository;
    private final SpringDataClientRepository springDataClientRepository;
    private final InvoicePersistenceMapper invoicePersistenceMapper;

    public InvoiceJpaAdapter(SpringDataInvoiceRepository springDataInvoiceRepository,
                             SpringDataClientRepository springDataClientRepository,
                             InvoicePersistenceMapper invoicePersistenceMapper) {
        this.springDataInvoiceRepository = springDataInvoiceRepository;
        this.springDataClientRepository = springDataClientRepository;
        this.invoicePersistenceMapper = invoicePersistenceMapper;
    }

    @Override
    @Transactional
    public Invoice save(Invoice invoice) {
        InvoiceEntity entity;

        if (invoice.getId() != null) {
            entity = springDataInvoiceRepository.findByIdWithLines(invoice.getId())
                    .orElseThrow(() -> new RuntimeException("Invoice not found for update: " + invoice.getId()));

            entity.setClient(springDataClientRepository.getReferenceById(invoice.getClientId()));
            entity.setDate(invoice.getDate());
            entity.setStatus(invoice.getStatus());
            entity.setDescription(invoice.getDescription());
            entity.setTotal(invoice.getTotal());
            entity.setTaxPercentage(invoice.getTaxPercentage());

            entity.getInvoiceLines().clear();

            for (InvoiceLineItem line : invoice.getLines()) {
                InvoiceLineEntity lineEntity = InvoiceLineEntity.builder()
                        .description(line.getDescription())
                        .quantity(line.getQuantity())
                        .unitPrice(line.getUnitPrice())
                        .total(line.getTotal())
                        .invoice(entity)
                        .build();
                entity.getInvoiceLines().add(lineEntity);
            }
        } else {
            entity = invoicePersistenceMapper.toNewEntity(invoice);
            entity.getInvoiceLines().forEach(line -> line.setInvoice(entity));
        }

        InvoiceEntity saved = springDataInvoiceRepository.save(entity);
        return invoicePersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<Invoice> findById(Long id) {
        return springDataInvoiceRepository.findByIdWithLines(id).map(invoicePersistenceMapper::toDomain);
    }

    @Override
    public List<Invoice> findAll() {
        return springDataInvoiceRepository.findAll().stream()
                .map(invoicePersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Invoice> findAllByClientId(Long clientId) {
        return springDataInvoiceRepository.findAllByClient_Id(clientId).stream()
                .map(invoicePersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        springDataInvoiceRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return springDataInvoiceRepository.existsById(id);
    }
}
