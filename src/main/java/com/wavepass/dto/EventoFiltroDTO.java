package com.wavepass.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EventoFiltroDTO(
        String nome,
        String genero,
        String categoria,
        LocalDate data,
        String local,
        String cidade,
        String estado,
        BigDecimal precoMax
){
}
