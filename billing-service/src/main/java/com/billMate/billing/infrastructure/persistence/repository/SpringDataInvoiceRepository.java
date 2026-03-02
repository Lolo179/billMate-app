package com.billMate.billing.infrastructure.persistence.repository;

import com.billMate.billing.infrastructure.persistence.entity.InvoiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpringDataInvoiceRepository extends JpaRepository<InvoiceEntity, Long> {

    List<InvoiceEntity> findAllByClient_Id(Long clientId);

    @Query("SELECT i FROM InvoiceEntity i LEFT JOIN FETCH i.invoiceLines WHERE i.invoiceId = :id")
    Optional<InvoiceEntity> findByIdWithLines(@Param("id") Long id);
}
