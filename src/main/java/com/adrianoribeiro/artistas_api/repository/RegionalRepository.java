package com.adrianoribeiro.artistas_api.repository;

import com.adrianoribeiro.artistas_api.model.Regional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegionalRepository extends JpaRepository<Regional, Long> {

    Optional<Regional> findByRegionalIdAndAtivoTrue(Integer regionalId);

    List<Regional> findAllByAtivoTrue();

    // Retorna o maior regionalId existente
    @Query("SELECT MAX(r.regionalId) FROM Regional r")
    Optional<Integer> findMaxRegionalId();
}