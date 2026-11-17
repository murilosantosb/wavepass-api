package com.wavepass.specification;

import com.wavepass.dto.EventoFiltroDTO;
import com.wavepass.model.Evento;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.stream.Stream;

public class EventoSpecification {

    public static Specification<Evento> comFiltros(EventoFiltroDTO filtro) {
        if (filtro == null) {
            return null;
        }

        return Stream.of(
                        comNome(filtro.nome()),
                        comGenero(filtro.genero()),
                        comCategoria(filtro.categoria()),
                        naData(filtro.data()),
                        naCidade(filtro.cidade()),
                        noEstado(filtro.estado()),
                        comPrecoMaximo(filtro.precoMax())
                )
                .filter(Objects::nonNull)
                .reduce(Specification::and)
                .orElse(null);
    }

    private static Specification<Evento> comNome(String nome){

        if (nome == null || nome.isBlank()){
            return null;
        }

        return (root, query, cb) ->
                cb.like(cb.lower(root.get("nome")), "%" + nome.trim().toLowerCase() + "%");
    }

    private static Specification<Evento> comGenero(String genero){
        if (genero == null || genero.isBlank()){
            return null;
        }
        return (root, query, cb) ->
            cb.like(cb.lower(root.get("genero")), "%" + genero.trim().toLowerCase() + "%");
    }

    private static Specification<Evento> comCategoria(String categoria) {
        if(categoria == null || categoria.isBlank()){
            return null;
        }

        return(root, query, cb) ->
                cb.like(cb.lower(root.get("categoria")), "%" + categoria.trim().toLowerCase() + "%");
    }

    private static Specification<Evento> naCidade(String cidade) {
        if(cidade == null || cidade.isBlank()){
            return null;
        }

        return(root, query, cb) ->
                cb.like(cb.lower(root.get("cidade")),"%" + cidade.trim().toLowerCase() + "%");
    }

    private static Specification<Evento> noEstado(String estado) {
        if(estado == null || estado.isBlank()){
            return null;
        }

        return(root, query, cb) ->
                cb.equal(cb.upper(root.get("estado")), estado.trim().toUpperCase() );
    }

    private static Specification<Evento> naData(LocalDate data) {
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

    private static Specification<Evento> comPrecoMaximo(BigDecimal precoMax) {
        if(precoMax == null){
            return null;
        }

        return(root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("precoMinimo"), precoMax);
    }

}






