package com.adrianoribeiro.artistas_api.config;

import com.adrianoribeiro.artistas_api.service.RegionalService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppStartupConfigRegional {
    @Bean
    ApplicationRunner runOnStartup(RegionalService regionalService) {
        return args -> regionalService.sincronizarRegionais();
    }
}
