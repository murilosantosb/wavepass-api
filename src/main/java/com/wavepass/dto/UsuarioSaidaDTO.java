package com.wavepass.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.wavepass.model.PerfilUsuario;
import com.wavepass.model.Usuario;

import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UsuarioSaidaDTO(
        Long id,
        String nome,
        String email,
        String telefone,
        String cpf,
        LocalDate dataNascimento,
        String cnpj,
        String nomeEmpresa,
        PerfilUsuario perfil
) {
    public static UsuarioSaidaDTO doModelo(Usuario usuario) {
        return new UsuarioSaidaDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getTelefone(),
                usuario.getCpf(),
                usuario.getDataNascimento(),
                usuario.getCnpj(),
                usuario.getNomeEmpresa(),
                usuario.getPerfil()
        );
    }
}