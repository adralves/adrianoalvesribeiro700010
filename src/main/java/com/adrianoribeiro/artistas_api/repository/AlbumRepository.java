package com.adrianoribeiro.artistas_api.repository;

import com.adrianoribeiro.artistas_api.model.Album;
import com.adrianoribeiro.artistas_api.model.enums.TipoArtista;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {
    Page<Album> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    @Query("""
        select distinct a
        from Album a
        join a.artistas ar
        where ar.tipo = :tipo
    """)
    Page<Album> findByTipoArtista(@Param("tipo") TipoArtista tipo, Pageable pageable);

    @Query("""
    SELECT a
    FROM Album a
    JOIN a.artistas ar
    WHERE ar.id = :artistaId
    """)
    List<Album> findAlbunsByArtistaId(@Param("artistaId") Long artistaId);

    @Query("""
        SELECT DISTINCT a
        FROM Album a
        JOIN a.artistas ar
        WHERE (:artista IS NULL
OR LOWER(ar.nome) LIKE LOWER(CONCAT('%', CAST(:artista AS string), '%')))
    """)
    Page<Album> listaAlbunsPorArtista(@Param("artista") String artista, Pageable pageable);

}
