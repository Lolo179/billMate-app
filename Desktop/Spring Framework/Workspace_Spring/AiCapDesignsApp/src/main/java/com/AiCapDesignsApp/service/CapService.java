package com.AiCapDesignsApp.service;

import java.util.Optional;

import com.AiCapDesignsApp.entity.Cap;

public interface CapService {

	public Cap save(Cap cap);
	public Optional<Cap> get(Integer id);
	public void update(Cap cap);
	public void delete(Integer id);
}
