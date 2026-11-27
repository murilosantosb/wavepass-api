package com.wavepass.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChatMensagemDTO(
        @NotBlank(message = "O tipo da mensagem é obrigatório")
        @Pattern(regexp = "USER|ASSISTANT", message = "O tipo deve ser USER ou ASSISTANT")
        String tipo,

        @NotBlank(message = "O conteúdo da mensagem é obrigatório")
        @Size(max = 12000, message = "A mensagem deve ter no máximo 12000 caracteres")
        String conteudo
) {}
