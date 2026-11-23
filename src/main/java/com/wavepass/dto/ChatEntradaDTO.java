package com.wavepass.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatEntradaDTO(
        @NotBlank(message = "A pergunta é obrigatória")
        @Size(max = 1000, message = "A pergunta deve ter no máximo 1000 caracteres")
        String pergunta
) {}
