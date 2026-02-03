package com.adrianoribeiro.artistas_api.controller;

import com.adrianoribeiro.artistas_api.dto.AtualizarRegionalRequestDTO;
import com.adrianoribeiro.artistas_api.dto.CriarRegionalRequestDTO;
import com.adrianoribeiro.artistas_api.dto.RegionalResponseDTO;
import com.adrianoribeiro.artistas_api.model.Regional;
import com.adrianoribeiro.artistas_api.service.RegionalQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

import java.util.List;

@RestController
@RequestMapping("/api/v1/regionais")
@Tag(
        name = "Regionais",
        description = "Endpoints para gerenciamento de regionais"
)
@SecurityRequirement(name = "bearerAuth")
public class RegionalController {

    private final RegionalQueryService service;

    public RegionalController(RegionalQueryService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(
            summary = "Listar regionais ativas"
    )
    public ResponseEntity<List<RegionalResponseDTO>> listar() {

        List<RegionalResponseDTO> response = service.listarAtivas()
                .stream()
                .map(RegionalResponseDTO::fromEntity)
                .toList();

        return ResponseEntity.ok(response);
    }

    @PostMapping
    @Operation(summary = "Criar nova regional")
    public ResponseEntity<RegionalResponseDTO> criar(@RequestBody CriarRegionalRequestDTO request) {
        Regional regional = service.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RegionalResponseDTO.fromEntity(regional));
    }

    @GetMapping("/{regionalId}")
    @Operation(summary = "Buscar regional por ID")
    public ResponseEntity<RegionalResponseDTO> buscarPorId(@PathVariable Integer regionalId) {
        Regional regional = service.buscarPorId(regionalId);
        return ResponseEntity.ok(RegionalResponseDTO.fromEntity(regional));
    }

    @PutMapping("/{regionalId}")
    @Operation(summary = "Atualizar nome de uma regional")
    public ResponseEntity<RegionalResponseDTO> atualizar(
            @PathVariable Integer regionalId,
            @RequestBody AtualizarRegionalRequestDTO request
    ) {
        Regional atualizado = service.atualizarRegional(regionalId, request);
        return ResponseEntity.ok(RegionalResponseDTO.fromEntity(atualizado));
    }


}