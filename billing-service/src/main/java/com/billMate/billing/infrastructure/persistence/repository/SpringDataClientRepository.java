package com.billMate.billing.infrastructure.persistence.repository;

import com.billMate.billing.entity.ClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataClientRepository extends JpaRepository<ClientEntity, Long> {

    boolean existsByNif(String nif);

    Optional<ClientEntity> findByNif(String nif);
}
