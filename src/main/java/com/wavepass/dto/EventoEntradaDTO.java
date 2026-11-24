package com.wavepass.dto;

import com.wavepass.model.CategoriaEvento;
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

        @NotNull(message = "A categoria é obrigatória")
        CategoriaEvento categoria,

        int faixaEtaria,

        @NotEmpty(message = "Informe pelo menos uma data para o evento")
        List<
                @NotNull(message = "A data não pode ser nula")
                @FutureOrPresent(message = "A data do evento deve ser no presente ou futuro")
                        LocalDateTime> datas,

        @NotBlank(message = "O local é obrigatório")
        String local,

        @NotBlank(message = "A cidade é obrigatória")
        String cidade,

        @NotBlank(message = "O estado é obrigatório")
        @Pattern(
                regexp = "^(AC|AL|AP|AM|BA|CE|DF|ES|GO|MA|MT|MS|MG|PA|PB|PR|PE|PI|RJ|RN|RS|RO|RR|SC|SP|SE|TO)$",
                message = "Estado inválido. Informe a sigla (ex: SP, RJ, MG)"
        )
        String estado,

        @NotNull(message = "O preço mínimo é obrigatório")
        @PositiveOrZero(message = "O preço mínimo não pode ser negativo")
        BigDecimal precoMinimo,

        String corTema,
        String imagemBanner,
        String imagemCard,
        String descricaoCurta,
        String descricaoCompleta,

        @Valid
        @NotEmpty(message = "O evento deve ter pelo menos um setor")
        List<SetorEntradaDTO> setores
) {}