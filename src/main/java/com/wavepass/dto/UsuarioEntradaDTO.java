package com.wavepass.dto;

import com.wavepass.model.PerfilUsuario;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record UsuarioEntradaDTO(
        @NotBlank(message = "O nome é obrigatório")
        String nome,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "Formato de e-mail inválido")
        String email,

        @Size(min = 11, max = 11, message = "O CPF deve ter exatamente 11 dígitos")
        @Pattern(regexp = "\\d{11}", message = "O CPF deve conter apenas números")
        String cpf,

        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 6, message = "A senha deve ter pelo menos 6 caracteres")
        String senha,

        String telefone,

        @Past(message = "A data de nascimento deve ser no passado")
        LocalDate dataNascimento,

        @Size(min = 14, max = 14, message = "O CNPJ deve ter exatamente 14 dígitos")
        @Pattern(regexp = "\\d{14}", message = "O CNPJ deve conter apenas números")
        String cnpj,

        String nomeEmpresa,

        PerfilUsuario perfil
) {}