package com.billMate.billing.infrastructure.persistence.adapter;

import com.billMate.billing.domain.invoice.model.Invoice;
import com.billMate.billing.domain.invoice.model.InvoiceLineItem;
import com.billMate.billing.domain.invoice.port.out.InvoiceRepositoryPort;
import com.billMate.billing.domain.shared.PageResult;
import com.billMate.billing.infrastructure.persistence.entity.InvoiceEntity;
import com.billMate.billing.infrastructure.persistence.entity.InvoiceLineEntity;
import com.billMate.billing.infrastructure.persistence.mapper.InvoicePersistenceMapper;
import com.billMate.billing.infrastructure.persistence.repository.SpringDataInvoiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Component
public class InvoiceJpaAdapter implements InvoiceRepositoryPort {

    private static final Logger log = LoggerFactory.getLogger(InvoiceJpaAdapter.class);
    private final SpringDataInvoiceRepository springDataInvoiceRepository;
    private final InvoicePersistenceMapper invoicePersistenceMapper;

    public InvoiceJpaAdapter(SpringDataInvoiceRepository springDataInvoiceRepository,
                             InvoicePersistenceMapper invoicePersistenceMapper) {
        this.springDataInvoiceRepository = springDataInvoiceRepository;
        this.invoicePersistenceMapper = invoicePersistenceMapper;
    }

    @Override
    @Transactional
    public Invoice save(Invoice invoice) {
        log.debug("Persisting invoice", kv("invoiceId", invoice.getId()));
        InvoiceEntity entity;

        if (invoice.getId() != null) {
            entity = springDataInvoiceRepository.findByIdWithLines(invoice.getId())
                    .orElseThrow(() -> new RuntimeException("Invoice not found for update: " + invoice.getId()));

            entity.setClientId(invoice.getClientId());
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
        log.debug("Invoice persisted", kv("invoiceId", saved.getInvoiceId()));
        return invoicePersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<Invoice> findById(Long id) {
        log.debug("Querying invoice in DB", kv("invoiceId", id));
        return springDataInvoiceRepository.findByIdWithLines(id).map(invoicePersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Invoice> findAll(int page, int size) {
        log.debug("Querying invoices in DB", kv("page", page), kv("size", size));
        Page<Long> invoiceIds = springDataInvoiceRepository.findPageIds(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt", "invoiceId"))
        );

        return mapInvoicePage(invoiceIds);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Invoice> findAllByClientId(Long clientId, int page, int size) {
        log.debug("Querying invoices in DB by client", kv("clientId", clientId), kv("page", page), kv("size", size));
        Page<Long> invoiceIds = springDataInvoiceRepository.findPageIdsByClientId(
                clientId,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt", "invoiceId"))
        );

        return mapInvoicePage(invoiceIds);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        log.debug("Deleting invoice from DB", kv("invoiceId", id));
        springDataInvoiceRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        log.debug("Checking invoice existence", kv("invoiceId", id));
        return springDataInvoiceRepository.existsById(id);
    }

    private PageResult<Invoice> mapInvoicePage(Page<Long> invoiceIdsPage) {
        if (invoiceIdsPage.isEmpty()) {
            return new PageResult<>(List.of(), invoiceIdsPage.getNumber(), invoiceIdsPage.getSize(), 0, invoiceIdsPage.getTotalPages());
        }

        List<Long> invoiceIds = invoiceIdsPage.getContent();
        Map<Long, Invoice> invoicesById = springDataInvoiceRepository.findAllByInvoiceIdInWithLines(invoiceIds).stream()
                .map(invoicePersistenceMapper::toDomain)
                .collect(java.util.stream.Collectors.toMap(Invoice::getId, Function.identity()));

        List<Invoice> orderedInvoices = invoiceIds.stream()
                .map(invoicesById::get)
                .filter(java.util.Objects::nonNull)
                .toList();

        return new PageResult<>(
                orderedInvoices,
                invoiceIdsPage.getNumber(),
                invoiceIdsPage.getSize(),
                invoiceIdsPage.getTotalElements(),
                invoiceIdsPage.getTotalPages()
        );
    }
}
