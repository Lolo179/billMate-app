package com.billMate.billing.domain.invoice.port.out;

import com.billMate.billing.domain.invoice.model.Invoice;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepositoryPort {

    Invoice save(Invoice invoice);

    Optional<Invoice> findById(Long id);

    List<Invoice> findAll();

    List<Invoice> findAllByClientId(Long clientId);

    void deleteById(Long id);

    boolean existsById(Long id);
}
