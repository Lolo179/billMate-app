package com.AiCapDesignsApp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.AiCapDesignsApp.entity.Cap;

@Repository
public interface CapRepository extends JpaRepository<Cap, Integer>{

}
