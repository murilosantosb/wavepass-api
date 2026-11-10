package com.wavepass.dto;

import com.wavepass.model.PerfilUsuario;
import com.wavepass.model.Usuario;

import java.time.LocalDate;

public record UsuarioSaidaDTO(
        Long id,
        String nome,
        String telefone,
        String cpf,
        LocalDate dataNascimento,
        String email,
        PerfilUsuario perfil
) {
    public static UsuarioSaidaDTO doModelo(Usuario usuario) {
        return new UsuarioSaidaDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getTelefone(),
                usuario.getCpf(),
                usuario.getDataNascimento(),
                usuario.getEmail(),
                usuario.getPerfil()
        );
    }
}