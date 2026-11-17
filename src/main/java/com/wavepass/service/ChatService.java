package com.wavepass.service;

import com.wavepass.repository.ChatRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class ChatService {
    private final ChatRepository chatRepository;
    private final ChatClient chatClient;

    public ChatService(ChatRepository chatRepository, ChatClient.Builder chatClient) {
        this.chatRepository = chatRepository;
        this.chatClient = chatClient.build();
    }

    public String chamadaAPI(String pergunta) {

        return chatClient.prompt()
                .system("Você é um assistente virtual do WavePass, sistema de eventos. Seu objetivo é informar sobre eventos aos clientes. Caso não haja eventos, diga apenas que não foi encontrado. Haja de forma educada e entusiasmada.")
                .user(pergunta)
                .call()
                .content();
    }
}
