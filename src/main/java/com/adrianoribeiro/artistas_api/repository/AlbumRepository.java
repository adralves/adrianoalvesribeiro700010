package com.adrianoribeiro.artistas_api.repository;

import com.adrianoribeiro.artistas_api.model.Album;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {
}
