package com.adrianoribeiro.artistas_api.repository;

import com.adrianoribeiro.artistas_api.model.AlbumImagem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlbumImagemRepository extends JpaRepository<AlbumImagem, Long> {
}
