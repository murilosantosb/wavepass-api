package com.wavepass.dto;

import com.wavepass.model.CategoriaEvento;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EventoAiDTO(
        Long id,
        String nome,
        String artistas,
        CategoriaEvento categoria,
        LocalDateTime dataHora,
        String local,
        BigDecimal precoMinimo,
        int faixaEtaria
) {}
