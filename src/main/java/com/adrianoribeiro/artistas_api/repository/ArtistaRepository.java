package com.adrianoribeiro.artistas_api.repository;

import com.adrianoribeiro.artistas_api.model.Artista;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

@Repository
public interface ArtistaRepository extends JpaRepository<Artista, Long> {

    Page<Artista> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

}
