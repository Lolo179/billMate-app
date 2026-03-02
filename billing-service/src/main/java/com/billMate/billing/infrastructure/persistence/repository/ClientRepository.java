package com.billMate.billing.infrastructure.persistence.repository;

import com.billMate.billing.infrastructure.persistence.entity.ClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends JpaRepository<ClientEntity,Long> {

}
