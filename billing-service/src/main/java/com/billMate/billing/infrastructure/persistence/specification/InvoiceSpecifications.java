package com.billMate.billing.infrastructure.persistence.specification;

import com.billMate.billing.domain.invoice.port.in.query.InvoiceSearchQuery;
import com.billMate.billing.infrastructure.persistence.entity.InvoiceEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Especificaciones JPA para búsquedas dinámicas de facturas.
 *
 * Seguridad: el campo sortField se valida con whitelist en GetAllInvoicesService
 * y GetInvoicesByClientService para prevenir inyección a través del parámetro de ordenación.
 */
public class InvoiceSpecifications {

    private InvoiceSpecifications() {
    }

    /**
     * Construye una Specification combinada a partir de los filtros del query.
     * El campo clientId es opcional: si está presente filtra por cliente (usado en /invoices/client/{id}).
     */
    public static Specification<InvoiceEntity> build(InvoiceSearchQuery query) {
        return (root, criteriaQuery, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filtro por clientId (usado cuando se llama desde GetInvoicesByClientUseCase)
            if (query.clientId() != null) {
                predicates.add(cb.equal(root.get("clientId"), query.clientId()));
            }

            // Filtro por estado exacto
            if (query.status() != null) {
                predicates.add(cb.equal(root.get("status"), query.status()));
            }

            // Filtro por fecha >= dateFrom
            if (query.dateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("date"), query.dateFrom()));
            }

            // Filtro por fecha <= dateTo
            if (query.dateTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("date"), query.dateTo()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
