package com.adrianoribeiro.artistas_api.service;

import com.adrianoribeiro.artistas_api.repository.ArtistaRepository;
import com.adrianoribeiro.artistas_api.model.Artista;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ArtistaService {

    @Autowired
    private ArtistaRepository artistaRepository;

    public Artista criarArtista(Artista artista){
        return artistaRepository.save(artista);
    }

    public List<Artista> listarArtista(){
        return artistaRepository.findAll();
    }

    public Page<Artista> listar(String nome,int pagina,int tamanho,String ordem) {

        Sort sort = ordem.equalsIgnoreCase("desc")
                ? Sort.by("nome").descending()
                : Sort.by("nome").ascending();

        Pageable pageable = PageRequest.of(pagina, tamanho, sort);

        if (nome == null || nome.isBlank()) {
            return artistaRepository.findAll(pageable);
        }

        return artistaRepository.findByNomeContainingIgnoreCase(nome, pageable);
    }

}
