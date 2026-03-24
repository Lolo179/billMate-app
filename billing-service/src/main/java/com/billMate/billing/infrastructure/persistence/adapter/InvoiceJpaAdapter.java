package com.billMate.billing.infrastructure.persistence.adapter;

import com.billMate.billing.domain.invoice.model.Invoice;
import com.billMate.billing.domain.invoice.model.InvoiceLineItem;
import com.billMate.billing.domain.invoice.port.in.query.InvoiceSearchQuery;
import com.billMate.billing.domain.invoice.port.out.InvoiceRepositoryPort;
import com.billMate.billing.domain.shared.PageResult;
import com.billMate.billing.infrastructure.persistence.entity.InvoiceEntity;
import com.billMate.billing.infrastructure.persistence.entity.InvoiceLineEntity;
import com.billMate.billing.infrastructure.persistence.mapper.InvoicePersistenceMapper;
import com.billMate.billing.infrastructure.persistence.repository.SpringDataInvoiceRepository;
import com.billMate.billing.infrastructure.persistence.specification.InvoiceSpecifications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    public PageResult<Invoice> search(InvoiceSearchQuery query) {
        log.debug("Searching invoices in DB",
                kv("page", query.page()), kv("size", query.size()),
                kv("sort", query.sortField() + "," + query.sortDir()),
                kv("status", query.status()), kv("clientId", query.clientId()));

        Specification<InvoiceEntity> spec = InvoiceSpecifications.build(query);
        Sort sort = buildSort(query.sortField(), query.sortDir());

        /*
         * Estrategia de paginación en dos pasos para evitar HHH90003004:
         * Hibernate no puede combinar JOIN FETCH (para cargar invoiceLines) con LIMIT/OFFSET.
         * Paso 1: paginamos sobre InvoiceEntity sin cargar las líneas (solo IDs).
         * Paso 2: cargamos las entidades completas con JOIN FETCH usando los IDs obtenidos.
         */
        Page<InvoiceEntity> invoicePage = springDataInvoiceRepository.findAll(
                spec, PageRequest.of(query.page(), query.size(), sort));

        if (invoicePage.isEmpty()) {
            return new PageResult<>(List.of(), invoicePage.getNumber(),
                    invoicePage.getSize(), 0L, invoicePage.getTotalPages());
        }

        List<Long> ids = invoicePage.getContent().stream()
                .map(InvoiceEntity::getInvoiceId)
                .toList();

        Map<Long, Invoice> invoicesById = springDataInvoiceRepository
                .findAllByInvoiceIdInWithLines(ids).stream()
                .map(invoicePersistenceMapper::toDomain)
                .collect(Collectors.toMap(Invoice::getId, Function.identity()));

        // Preservar el orden de la página original
        List<Invoice> orderedInvoices = ids.stream()
                .map(invoicesById::get)
                .filter(Objects::nonNull)
                .toList();

        log.debug("Invoices found", kv("count", orderedInvoices.size()),
                kv("totalElements", invoicePage.getTotalElements()));

        return new PageResult<>(orderedInvoices, invoicePage.getNumber(),
                invoicePage.getSize(), invoicePage.getTotalElements(), invoicePage.getTotalPages());
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

    /**
     * Construye un Sort de Spring Data a partir de los campos del query.
     * El campo sortField ya viene validado desde la capa de aplicación.
     */
    private Sort buildSort(String sortField, String sortDir) {
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir)
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(direction, sortField);
    }
}

