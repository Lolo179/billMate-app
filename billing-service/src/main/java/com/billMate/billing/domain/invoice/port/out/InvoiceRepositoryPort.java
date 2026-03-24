package com.billMate.billing.domain.invoice.port.out;

import com.billMate.billing.domain.invoice.model.Invoice;
import com.billMate.billing.domain.invoice.port.in.query.InvoiceSearchQuery;
import com.billMate.billing.domain.shared.PageResult;

import java.util.Optional;

public interface InvoiceRepositoryPort {

    Invoice save(Invoice invoice);

    Optional<Invoice> findById(Long id);

    PageResult<Invoice> search(InvoiceSearchQuery query);

    void deleteById(Long id);

    boolean existsById(Long id);
}
