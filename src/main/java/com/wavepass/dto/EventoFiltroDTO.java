package com.wavepass.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EventoFiltroDTO(
        String nome,
        String genero,
        String categoria,
        String localizacao,
        BigDecimal precoMin,
        BigDecimal precoMax
){
}
