package com.adrianoribeiro.artistas_api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@FeignClient(name = "regionalClient", url = "https://integrador-argus-api.geia.vip/v1")
public interface RegionalClient {

    @GetMapping("/regionais")
    List<RegionalDto> buscarRegionais();

    // DTO interno do Feign
    class RegionalDto {
        private Integer id;
        private String nome;

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }

        public String getNome() { return nome; }
        public void setNome(String nome) { this.nome = nome; }
    }
}
