package com.billMate.billing.domain.client.port.in.query;

/**
 * Parámetros de búsqueda paginada para clientes.
 *
 * @param page      número de página (0-based)
 * @param size      tamaño de página (1-20)
 * @param sortField campo de ordenación (whitelist: name, email, nif, createdAt)
 * @param sortDir   dirección de ordenación (asc | desc)
 * @param name      filtro parcial por nombre (ILIKE)
 * @param nif       filtro exacto por NIF
 */
public record ClientSearchQuery(
        int page,
        int size,
        String sortField,
        String sortDir,
        String name,
        String nif) {
}
