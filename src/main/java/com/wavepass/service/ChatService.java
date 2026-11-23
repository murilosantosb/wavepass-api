package com.wavepass.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class ChatService {
    private final ChatClient chatClient;

    public ChatService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String chamadaAPI(String pergunta) {

        return chatClient.prompt()
                .user(pergunta)
                .call()
                .content();
    }
}
