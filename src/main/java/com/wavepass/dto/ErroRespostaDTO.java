package com.wavepass.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErroRespostaDTO(
        LocalDateTime timestamp,
        Integer status,
        String mensagem,
        String caminho,
        List<CampoErroDTO> erros
) {
    public record CampoErroDTO(String campo, String mensagem) {}
}