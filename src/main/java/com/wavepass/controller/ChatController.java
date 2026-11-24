package com.wavepass.controller;

import com.wavepass.dto.ChatEntradaDTO;
import com.wavepass.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wavepass/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/mensagens")
    public ResponseEntity<String> chamadaAPI(@RequestBody @Valid ChatEntradaDTO entrada) {

        return ResponseEntity.ok(chatService.chamadaAPI(entrada));
    }
}
