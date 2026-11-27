package com.wavepass.controller;

import com.wavepass.dto.ChatEntradaDTO;
import com.wavepass.dto.ChatRespostaDTO;
import com.wavepass.model.Usuario;
import com.wavepass.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wavepass/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/mensagens")
    public ResponseEntity<ChatRespostaDTO> responderPergunta(@RequestBody @Valid ChatEntradaDTO entrada,
                                                              @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(chatService.responderPergunta(entrada, usuario));
    }
}
