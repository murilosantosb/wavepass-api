package com.wavepass.model;

import java.util.Locale;

public enum CategoriaEvento {
    SHOW,
    TEATRO,
    FESTA,
    EXPOSICAO,
    ESPORTES,
    FILMES,
    GASTRONOMIA,
    GAMES,
    OUTROS;

    public static CategoriaEvento porDescricao(String descricao) {
        String categoria = descricao.strip().toUpperCase(Locale.ROOT);
        return "CINEMA".equals(categoria) ? FILMES : valueOf(categoria);
    }
}

