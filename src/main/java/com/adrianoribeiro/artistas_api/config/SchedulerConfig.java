package com.adrianoribeiro.artistas_api.config;

import com.adrianoribeiro.artistas_api.service.RegionalService;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class SchedulerConfig {

    private final RegionalService regionalService;

    public SchedulerConfig(RegionalService regionalService) {
        this.regionalService = regionalService;
    }

    @Scheduled(
            initialDelay = 3_000,   // Roda 5 segundos após subir a aplicação
            fixedRate = 60_000
    )
    public void sincronizarPeriodicamente() {
        regionalService.sincronizarRegionais();
    }
}