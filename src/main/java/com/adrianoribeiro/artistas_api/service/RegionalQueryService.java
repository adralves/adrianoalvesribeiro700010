package com.adrianoribeiro.artistas_api.service;

import com.adrianoribeiro.artistas_api.model.Regional;
import com.adrianoribeiro.artistas_api.repository.RegionalRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class RegionalQueryService {

    private final RegionalRepository repository;

    public RegionalQueryService(RegionalRepository repository) {
        this.repository = repository;
    }

    public List<Regional> listarAtivas() {
        return repository.findAllByAtivoTrue();
    }
}
