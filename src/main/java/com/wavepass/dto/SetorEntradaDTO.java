package com.wavepass.dto;

import com.wavepass.model.TipoSetor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record SetorEntradaDTO (
        @NotNull(message = "O tipo do setor é obrigatório")
        TipoSetor tipo,

        @NotNull(message = "O preço é obrigatório")
        @Positive(message = "O preço do setor deve ser maior que zero")
        BigDecimal preco
){
}
