package com.billMate.billing.repository.JpaRepository;

import com.billMate.billing.entity.ClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<ClientEntity,Long> {

}
