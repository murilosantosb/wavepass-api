package com.wavepass.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ChatEntradaDTO(
        @NotBlank(message = "A pergunta é obrigatória")
        @Size(max = 1000, message = "A pergunta deve ter no máximo 1000 caracteres")
        String pergunta,

        @Size(max = 20, message = "O histórico deve ter no máximo 20 mensagens")
        List<@NotNull @Valid ChatMensagemDTO> historico
) {
    public ChatEntradaDTO {
        historico = historico == null ? List.of() : historico;
    }

    @AssertTrue(message = "O histórico deve ter no máximo 40000 caracteres")
    public boolean isHistoricoDentroDoLimite() {
        return historico.stream()
                .filter(mensagem -> mensagem != null && mensagem.conteudo() != null)
                .mapToLong(mensagem -> mensagem.conteudo().length())
                .sum() <= 40000;
    }
}
