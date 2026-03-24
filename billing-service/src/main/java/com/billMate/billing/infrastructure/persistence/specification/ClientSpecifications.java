package com.billMate.billing.infrastructure.persistence.specification;

import com.billMate.billing.domain.client.port.in.query.ClientSearchQuery;
import com.billMate.billing.infrastructure.persistence.entity.ClientEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Especificaciones JPA para búsquedas dinámicas de clientes.
 * Cada método devuelve un predicado que se combina con Specification.where().and().
 *
 * Seguridad: el campo sortField se valida con whitelist en GetAllClientsService
 * para prevenir inyección a través del parámetro de ordenación.
 */
public class ClientSpecifications {

    private ClientSpecifications() {
    }

    /**
     * Construye una Specification combinada a partir de los filtros del query.
     */
    public static Specification<ClientEntity> build(ClientSearchQuery query) {
        return (root, criteriaQuery, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filtro por nombre (ILIKE — búsqueda parcial insensible a mayúsculas)
            if (query.name() != null && !query.name().isBlank()) {
                predicates.add(
                        cb.like(cb.lower(root.get("name")), "%" + query.name().toLowerCase() + "%")
                );
            }

            // Filtro por NIF exacto (case-sensitive: los NIF tienen formato estricto)
            if (query.nif() != null && !query.nif().isBlank()) {
                predicates.add(cb.equal(root.get("nif"), query.nif()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
