package com.wavepass.specification;

import com.wavepass.dto.EventoFiltroDTO;
import com.wavepass.model.CategoriaEvento;
import com.wavepass.model.Evento;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.stream.Stream;

public class EventoSpecification {

    public static Specification<Evento> comFiltros(EventoFiltroDTO filtro) {
        if (filtro == null) return null;

        return Stream.of(
                        comNome(filtro.nome()),
                        comGenero(filtro.genero()),
                        comCategoria(filtro.categoria()),
                        naLocalizacao(filtro.localizacao()),
                        comPrecoMinimo(filtro.precoMin()),
                        comPrecoMaximo(filtro.precoMax())
                )
                .filter(Objects::nonNull)
                .reduce(Specification::and)
                .orElse(null);
    }

    private static Specification<Evento> comNome(String nome){

        if (nome == null || nome.isBlank()) return null;

        return (root, query, cb) ->
                cb.like(cb.lower(root.get("nome")), "%" + nome.trim().toLowerCase() + "%");
    }

    private static Specification<Evento> comGenero(String genero){
        if (genero == null || genero.isBlank()) return null;

        return (root, query, cb) ->
            cb.like(cb.lower(root.get("genero")), "%" + genero.trim().toLowerCase() + "%");
    }

    private static Specification<Evento> comCategoria(CategoriaEvento categoria) {
        if(categoria == null) return null;

        return(root, query, cb) ->
                cb.equal(root.get("categoria"), categoria);
    }

    private static Specification<Evento> naLocalizacao(String localizacao) {
        if(localizacao == null || localizacao.isBlank()) return null;

        String termo = "%" + localizacao.trim().toLowerCase() + "%";

        return(root, query, cb) ->
                cb.or(
                        cb.like(cb.lower(root.get("cidade")), termo),
                        cb.like(cb.lower(root.get("estado")), termo)
                );
    }

    private static Specification<Evento> noEstado(String estado) {
        if(estado == null || estado.isBlank()) return null;

        return(root, query, cb) ->
                cb.equal(cb.upper(root.get("estado")), estado.trim().toUpperCase() );
    }

    private static Specification<Evento> comPrecoMinimo(BigDecimal precoMin){
        if (precoMin == null) return null;

        return(root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("precoMinimo"), precoMin);
    }

    private static Specification<Evento> comPrecoMaximo(BigDecimal precoMax) {
        if(precoMax == null) return null;

        return(root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("precoMinimo"), precoMax);
    }
}






