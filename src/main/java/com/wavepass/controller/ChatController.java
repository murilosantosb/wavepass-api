package com.wavepass.controller;

import com.wavepass.model.Chat;
import com.wavepass.service.ChatService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wavepass/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/{pergunta}")
    public ResponseEntity<String> chamadaAPI(@PathVariable String pergunta) {

        return ResponseEntity.ok(chatService.chamadaAPI(pergunta));
    }
}
