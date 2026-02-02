package com.adrianoribeiro.artistas_api.controller;

import com.adrianoribeiro.artistas_api.service.RegionalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/regionais")
@Tag(
        name = "Regionais",
        description = "Endpoints responsáveis pela sincronização de regionais"
)
@SecurityRequirement(name = "bearerAuth")
public class RegionalSyncController {

    private final RegionalService service;

    public RegionalSyncController(RegionalService service) {
        this.service = service;
    }

    @PostMapping("/sincronizar")
    @Operation(
            summary = "Sincronizar regionais",
            description = """
            Sincroniza as regionais com base no endpoint externo.
            
            Observação:
            Além do endpoint manual, a aplicação executa a sincronização
            automaticamente a cada 60 segundos,
            garantindo que os dados permaneçam sempre atualizados.
            """
    )
    public ResponseEntity<Void> sincronizar() {
        service.sincronizarRegionais();
        return ResponseEntity.noContent().build();
    }
}