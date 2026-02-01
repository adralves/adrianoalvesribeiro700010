package com.adrianoribeiro.artistas_api.websocket;

import com.adrianoribeiro.artistas_api.dto.AlbumNotificacaoDTO;
import com.adrianoribeiro.artistas_api.model.Album;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class AlbumNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public AlbumNotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void notificarNovoAlbum(Album album) {

        AlbumNotificacaoDTO albumNotificacaoDTO = new AlbumNotificacaoDTO(
                album.getId(),
                album.getNome(),
                "Novo álbum cadastrado"
        );
        messagingTemplate.convertAndSend(
                "/topic/novo-album",
                albumNotificacaoDTO
        );
    }
}