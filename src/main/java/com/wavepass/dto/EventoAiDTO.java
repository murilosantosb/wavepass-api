package com.wavepass.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EventoAiDTO(
        Long id,
        String nome,
        String artistas,
        LocalDateTime dataHora,
        String local,
        BigDecimal precoMinimo,
        int faixaEtaria
) {}
