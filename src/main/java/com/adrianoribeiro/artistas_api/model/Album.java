package com.adrianoribeiro.artistas_api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "album")
public class Album {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @ManyToMany
    @JoinTable(
            name = "artista_album",
            joinColumns = @JoinColumn(name = "album_id"),
            inverseJoinColumns = @JoinColumn(name = "artista_id")
    )
    @JsonIgnore
    private Set<Artista> artistas = new HashSet<>();

    @OneToMany(
            mappedBy = "album",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<AlbumImagem> imagens = new ArrayList<>();

    public Album() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Set<Artista> getArtistas() {
        return artistas;
    }

    public void setArtistas(Set<Artista> artistas) {
        this.artistas = artistas;
    }

    public List<AlbumImagem> getImagens() {
        return imagens;
    }

    public void setImagens(List<AlbumImagem> imagens) {
        this.imagens = imagens;
    }
}
