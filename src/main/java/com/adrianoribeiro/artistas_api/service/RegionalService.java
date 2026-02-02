package com.adrianoribeiro.artistas_api.service;

import com.adrianoribeiro.artistas_api.client.RegionalClient.RegionalDto;
import com.adrianoribeiro.artistas_api.client.RegionalClient;
import com.adrianoribeiro.artistas_api.model.Regional;
import com.adrianoribeiro.artistas_api.repository.RegionalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RegionalService {

    private final RegionalRepository repository;
    private final RegionalClient client;

    public RegionalService(RegionalRepository repository, RegionalClient client) {
        this.repository = repository;
        this.client = client;
    }

    @Transactional
    public void sincronizarRegionais() {
        List<RegionalDto> externos = client.buscarRegionais();
        if (externos == null) return;

        List<Regional> internos = repository.findAll();

        Map<Integer, Regional> mapInternos = internos.stream()
                .collect(Collectors.toMap(Regional::getRegionalId, r -> r));

        Set<Integer> idsExternos = externos.stream()
                .map(RegionalDto::getId)
                .collect(Collectors.toSet());

        for (RegionalDto dto : externos) {
            Regional existente = mapInternos.get(dto.getId());

            if (existente == null) {
                // Novo registro
                Regional novo = new Regional();
                novo.setRegionalId(dto.getId());
                novo.setNome(dto.getNome());
                novo.setAtivo(true);
                repository.save(novo);
            } else if (!existente.getNome().equals(dto.getNome()) || !existente.getAtivo()) {
                // Alteração → inativa antigo e cria novo
                existente.setAtivo(false);
                repository.save(existente);

                Regional novo = new Regional();
                novo.setRegionalId(dto.getId());
                novo.setNome(dto.getNome());
                novo.setAtivo(true);
                repository.save(novo);
            }
        }

        // Inativa registros ausentes
        for (Regional interno : internos) {
            if (!idsExternos.contains(interno.getRegionalId()) && interno.getAtivo()) {
                interno.setAtivo(false);
                repository.save(interno);
            }
        }
    }
}