package com.billMate.billing.domain.invoice.port.in.query;

import com.billMate.billing.domain.invoice.model.InvoiceStatus;

import java.time.LocalDate;

/**
 * Parámetros de búsqueda paginada para facturas.
 *
 * @param page      número de página (0-based)
 * @param size      tamaño de página (1-20)
 * @param sortField campo de ordenación (whitelist: date, total, status, createdAt)
 * @param sortDir   dirección de ordenación (asc | desc)
 * @param status    filtro exacto por estado (nullable)
 * @param dateFrom  filtro fecha >= dateFrom (nullable)
 * @param dateTo    filtro fecha <= dateTo (nullable)
 * @param clientId  filtro por cliente (nullable; usado en /invoices/client/{clientId})
 */
public record InvoiceSearchQuery(
        int page,
        int size,
        String sortField,
        String sortDir,
        InvoiceStatus status,
        LocalDate dateFrom,
        LocalDate dateTo,
        Long clientId) {
}
