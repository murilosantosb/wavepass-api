package com.wavepass.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AdminDTO(

        @NotBlank(message = "A razão social ou nome da empresa é obrigatória")
        String nomeEmpresa,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "Formato de e-mail inválido")
        String email,

        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 6, message = "A senha deve ter pelo menos 6 caracteres")
        String senha,

        @NotBlank(message = "O CNPJ é obrigatório")
        @Size(min = 14, message = "O cnpj deve ter pelo menos 14 caracteres")
        String cnpj,

        String telefone

){
}
