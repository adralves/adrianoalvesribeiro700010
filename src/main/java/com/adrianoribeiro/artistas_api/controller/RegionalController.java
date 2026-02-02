package com.adrianoribeiro.artistas_api.controller;

import com.adrianoribeiro.artistas_api.dto.RegionalResponse;
import com.adrianoribeiro.artistas_api.service.RegionalQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/regionais")
@Tag(
        name = "Regionais",
        description = "Consulta de regionais ativas"
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
    public ResponseEntity<List<RegionalResponse>> listar() {

        List<RegionalResponse> response = service.listarAtivas()
                .stream()
                .map(RegionalResponse::fromEntity)
                .toList();

        return ResponseEntity.ok(response);
    }
}