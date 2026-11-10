package com.wavepass.dto;

import com.wavepass.model.PerfilUsuario;
import java.time.LocalDate;

public record UsuarioEntradaDTO(
         String nome,
         String telefone,
         String cpf,
         LocalDate dataNascimento,
         String email,
         String senha,
         PerfilUsuario perfil
) {
}

