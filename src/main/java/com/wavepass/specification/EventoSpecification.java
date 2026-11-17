package com.wavepass.specification;

import com.wavepass.dto.EventoFiltroDTO;
import com.wavepass.model.Evento;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class EventoSpecification {

    public static Specification<Evento> comFiltros(EventoFiltroDTO filtro){
        return Specification
                .where(nomeContem(filtro.nome()))
                .and(generoContem(filtro.genero()))
                .and(categoriaContem(filtro.categoria()))
                .and(cidadeContem(filtro.cidade()))
                .and(estadoIgual(filtro.estado()))
                .and(dataContem(filtro.data()))
                .and(precoMaxIgual(filtro.precoMax()));
    }

    private static Specification<Evento> nomeContem(String nome){

        if (nome == null || nome.isBlank()){
            return null;
        }

        return (root, query, cb) ->
                cb.like(cb.lower(root.get("nome")), "%" + nome.trim().toLowerCase() + "%");
    }

    private static Specification<Evento> generoContem(String genero){
        if (genero == null || genero.isBlank()){
            return null;
        }
        return (root, query, cb) ->
            cb.like(cb.lower(root.get("genero")), "%" + genero.trim().toLowerCase() + "%");
    }

    private static Specification<Evento> categoriaContem(String categoria) {
        if(categoria == null || categoria.isBlank()){
            return null;
        }

        return(root, query, cb) ->
                cb.like(cb.lower(root.get("categoria")), "%" + categoria.trim().toLowerCase() + "%");
    }

    private static Specification<Evento> cidadeContem(String cidade) {
        if(cidade == null || cidade.isBlank()){
            return null;
        }

        return(root, query, cb) ->
                cb.like(cb.lower(root.get("cidade")),"%" + cidade.trim().toLowerCase() + "%");
    }

    private static Specification<Evento> estadoIgual(String estado) {
        if(estado == null || estado.isBlank()){
            return null;
        }

        return(root, query, cb) ->
                cb.equal(cb.upper(root.get("estado")), estado.trim().toUpperCase() );
    }

    private static Specification<Evento> dataContem(LocalDate data) {
        if(data == null){
            return null;
        }

        return(root, query, cb) -> {
            LocalDateTime inicioDoDia = data.atStartOfDay();
            LocalDateTime fimDoDia = data.plusDays(1).atStartOfDay();

            return cb.and(
                    cb.greaterThanOrEqualTo(root.get("dataHora"), inicioDoDia),
                    cb.lessThan(root.get("dataHora"), fimDoDia)
            );
        };
    }

    private static Specification<Evento> precoMaxIgual(BigDecimal precoMax) {
        if(precoMax == null){
            return null;
        }

        return(root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("precoMinimo"), precoMax);
    }

}






