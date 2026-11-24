package com.wavepass.dto;

import jakarta.validation.constraints.*;

public record AdminEntradaDTO (
        @NotBlank(message = "O nome do responsável é obrigatório")
        String nome,

        @NotBlank(message = "O nome da empresa é obrigatório")
        String nomeEmpresa,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "Formato de e-mail inválido")
        String email,

        @Size(min = 14, max = 14, message = "O CNPJ deve ter exatamente 14 dígitos")
        @Pattern(regexp = "\\d{14}", message = "O CNPJ deve conter apenas números")
        String cnpj,

        String telefone,

        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 6, message = "A senha deve ter pelo menos 6 caracteres")
        String senha
) {}