package com.wavepass.dto;

import com.wavepass.model.CategoriaEvento;

import java.math.BigDecimal;

public record EventoFiltroDTO(
        String nome,
        String genero,
        CategoriaEvento categoria,
        String localizacao,
        BigDecimal precoMin,
        BigDecimal precoMax
){
}
