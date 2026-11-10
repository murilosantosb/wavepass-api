package com.wavepass.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record EventoEntradaDTO(
        String nome,
        List<String> artistas,
        String genero,
        String categoria,
        int faixaEtaria,
        LocalDateTime dataHora,
        String local,
        BigDecimal precoMinimo,
        String corTema,
        String imagemBanner,
        String imagemCard,
        String descricaoCurta,
        String descricaoCompleta,
        List<SetorEntradaDTO> setores
) {
}
