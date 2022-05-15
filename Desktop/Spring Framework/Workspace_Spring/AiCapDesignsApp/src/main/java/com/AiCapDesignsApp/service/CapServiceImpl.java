package com.AiCapDesignsApp.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.AiCapDesignsApp.entity.Cap;
import com.AiCapDesignsApp.repository.CapRepository;

@Service
public class CapServiceImpl implements CapService{

	@Autowired
	private CapRepository capRepository;
	
	@Override
	public Cap save(Cap cap) {
		
		return capRepository.save(cap);
	}

	@Override
	public Optional<Cap> get(Integer id) {
		return capRepository.findById(id);
	}

	@Override
	public void update(Cap cap) {
	capRepository.save(cap);
		
	}

	@Override
	public void delete(Integer id) {
		capRepository.deleteById(id);
	}

}
