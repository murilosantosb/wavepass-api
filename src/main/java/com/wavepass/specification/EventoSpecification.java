package com.wavepass.specification;

import com.wavepass.dto.EventoFiltroDTO;
import com.wavepass.model.CategoriaEvento;
import com.wavepass.model.Evento;
import com.wavepass.model.EstadoBrasil;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
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

    private static Specification<Evento> comCategoria(String categoria) {
        if (categoria == null || categoria.isBlank()) return null;

        try {
            CategoriaEvento enumCategoria = CategoriaEvento.porDescricao(categoria);
            return (root, query, cb) -> cb.equal(root.get("categoria"), enumCategoria);
        } catch (IllegalArgumentException e) {
            return (root, query, cb) -> cb.disjunction();
        }
    }

    public static Specification<Evento> naLocalizacao(String localizacao) {
        if(localizacao == null || localizacao.isBlank()) return null;

        String valor = EstadoBrasil.normalizar(localizacao);
        String termo = "%" + valor.replace("\\", "\\\\")
                .replace("%", "\\%").replace("_", "\\_") + "%";
        EstadoBrasil estado = EstadoBrasil.porNomeOuSigla(localizacao);

        return (root, query, cb) -> {
            var cidade = cb.like(semAcentos(cb, root.get("cidade")), termo, '\\');
            var estadoNormalizado = semAcentos(cb, root.get("estado"));
            if (estado != null) {
                return cb.or(cidade,
                        cb.equal(estadoNormalizado, EstadoBrasil.normalizar(estado.name())),
                        cb.equal(estadoNormalizado, estado.nomeNormalizado()));
            }
            return cb.or(cidade, cb.like(estadoNormalizado, termo, '\\'));
        };
    }

    private static Expression<String> semAcentos(CriteriaBuilder cb, Expression<String> campo) {
        Expression<String> resultado = cb.lower(cb.trim(campo));
        String acentuados = "áàâãäéèêëíìîïóòôõöúùûüç";
        String equivalentes = "aaaaaeeeeiiiiooooouuuuc";
        for (int i = 0; i < acentuados.length(); i++) {
            resultado = cb.function("replace", String.class, resultado,
                    cb.literal(String.valueOf(acentuados.charAt(i))),
                    cb.literal(String.valueOf(equivalentes.charAt(i))));
        }
        return resultado;
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






