package com.wavepass.dto;

import com.wavepass.model.PerfilUsuario;

public record TokenDTO(
        String token,
        Long id,
        String nome,
        PerfilUsuario perfil
) {}
