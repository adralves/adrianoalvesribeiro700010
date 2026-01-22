package com.adrianoribeiro.artistas_api.resource;

import com.adrianoribeiro.artistas_api.service.ArtistaService;
import com.adrianoribeiro.artistas_api.model.Artista;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/artistas")
public class ArtistaResource {

    @Autowired
    private ArtistaService artistaService;

/*    @GetMapping
    public List<Artista> listar() {
        return artistaService.listarArtista() ;
    }*/

    @PostMapping
    public Artista criarArtista(@RequestBody Artista artista) {
        return artistaService.criarArtista(artista);
    }

    @GetMapping
    public Page<Artista> listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanho,
            @RequestParam(defaultValue = "asc") String ordem,
            @RequestParam(required = false) String nome
    ) {
        return artistaService.listar(nome, pagina, tamanho, ordem);
    }
}
