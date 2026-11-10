package com.wavepass.dto;

import com.wavepass.model.TipoSetor;
import java.math.BigDecimal;

public record SetorEntradaDTO (
        TipoSetor tipo,
        BigDecimal preco
){
}
