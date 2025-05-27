package com.billMate.billing.repository;

import com.billMate.billing.entity.InvoiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<InvoiceEntity,Long> {

    List<InvoiceEntity> findAllByClient_Id(Long clientId);

}
