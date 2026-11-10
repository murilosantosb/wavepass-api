package com.wavepass.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record EventoEntradaDTO(
        @NotBlank(message = "O nome do evento é obrigatório")
        String nome,

        String artistas,

        @NotBlank(message = "O gênero é obrigatório")
        String genero,

        @NotBlank(message = "A categoria é obrigatória")
        String categoria,

        int faixaEtaria,

        @NotNull(message = "A data e hora são obrigatórias")
        @FutureOrPresent(message = "A data do evento deve ser no presente ou futuro")
        LocalDateTime dataHora,

        @NotBlank(message = "O local é obrigatório")
        String local,

        @NotNull(message = "O preço mínimo é obrigatório")
        @PositiveOrZero(message = "O preço mínimo não pode ser negativo")
        BigDecimal precoMinimo,

        String corTema,
        String imagemBanner,
        String imagemCard,
        String descricaoCurta,
        String descricaoCompleta,

        @Valid
        List<SetorEntradaDTO> setores
) {}