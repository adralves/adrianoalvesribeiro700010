package com.adrianoribeiro.artistas_api.service;

import com.adrianoribeiro.artistas_api.client.RegionalClient.RegionalDto;
import com.adrianoribeiro.artistas_api.client.RegionalClient;
import com.adrianoribeiro.artistas_api.model.Regional;
import com.adrianoribeiro.artistas_api.repository.RegionalRepository;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RegionalService {

    private final RegionalRepository repository;
    private final RegionalClient client;

    @Autowired
    private EntityManager em;

    public RegionalService(RegionalRepository repository, RegionalClient client) {
        this.repository = repository;
        this.client = client;
    }

    @Transactional
    public void sincronizarRegionais() {

        List<RegionalDto> externos = client.buscarRegionais();
        if (externos == null) return;

        // 1. Buscamos apenas os registros ATIVOS para comparação
        List<Regional> internosAtivos = repository.findAllByAtivoTrue();

        // 2. Criamos o Map sem risco de duplicidade (pois cada ID só deve ter um ativo)
        Map<Integer, Regional> mapAtivos = internosAtivos.stream()
                .collect(Collectors.toMap(
                        Regional::getRegionalId,
                        r -> r,
                        (existente, duplicado) -> existente // Segurança extra contra duplicatas inesperadas
                ));

        Set<Integer> idsExternos = externos.stream()
                .map(RegionalDto::getId)
                .collect(Collectors.toSet());

        // Processa inserções e atualizações
        for (RegionalDto dto : externos) {
            Regional existente = mapAtivos.get(dto.getId());

            if (existente == null) {
                // Regra 1: Novo no endpoint → inserir
                salvarNovaRegional(dto);
            } else if (!existente.getNome().equals(dto.getNome())) {
                // Regra 3: Atributo alterado → inativar antigo e criar novo
                existente.setAtivo(false);

                repository.saveAndFlush(existente); // garante que está no banco

                em.detach(existente); // remove do contexto de persistência

                salvarNovaRegional(dto);
            }
            // Se já existe e o nome é igual, não faz nada (já está ativo)
        }

        // Regra 2: Inativa registros ausentes no endpoint
        for (Regional ativo : internosAtivos) {
            if (!idsExternos.contains(ativo.getRegionalId())) {
                ativo.setAtivo(false);
                repository.save(ativo);
            }
        }
    }

    // Metodo auxiliar para evitar repetição de código
    private void salvarNovaRegional(RegionalDto dto) {

        Regional novo = new Regional();
        novo.setRegionalId(dto.getId());
        novo.setNome(dto.getNome());
        novo.setAtivo(true);
        repository.save(novo);

    }


}