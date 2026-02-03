package com.adrianoribeiro.artistas_api.service;

import com.adrianoribeiro.artistas_api.dto.AtualizarRegionalRequestDTO;
import com.adrianoribeiro.artistas_api.dto.CriarRegionalRequestDTO;
import com.adrianoribeiro.artistas_api.model.Regional;
import com.adrianoribeiro.artistas_api.repository.RegionalRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

    @Transactional
    public Regional criar(CriarRegionalRequestDTO request) {
        Regional regional = new Regional();
        regional.setRegionalId(request.regionalId());
        regional.setNome(request.nome());
        regional.setAtivo(Boolean.TRUE);
        return repository.save(regional);
    }

    public Regional buscarPorId(Integer regionalId) {
        return repository.findByRegionalIdAndAtivoTrue(regionalId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Regional não encontrada para o ID informado: " + regionalId
                ));
    }

    @Transactional
    public Regional atualizarRegional(Integer regionalId, AtualizarRegionalRequestDTO request) {
        // Busca registro ativo existente
        Optional<Regional> existenteOpt = repository.findByRegionalIdAndAtivoTrue(regionalId);

        if (existenteOpt.isPresent()) {
            Regional existente = existenteOpt.get();

            // Verifica se o nome mudou
            if (!existente.getNome().equals(request.nome())) {
                // Inativa o registro antigo
                existente.setAtivo(false);
                repository.save(existente);

                // Cria novo registro ativo
                Regional novo = new Regional();
                novo.setRegionalId(request.regionalId());
                novo.setNome(request.nome());
                novo.setAtivo(true);
                return repository.save(novo);
            } else {
                // Se o nome não mudou, apenas retorna o existente
                return existente;
            }
        } else {
            // Se não existe registro ativo, cria novo
            Regional novo = new Regional();
            novo.setRegionalId(request.regionalId());
            novo.setNome(request.nome());
            novo.setAtivo(true);
            return repository.save(novo);
        }
    }

}
