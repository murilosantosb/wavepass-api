package com.wavepass.service;

import com.wavepass.dto.EventoEntradaDTO;
import com.wavepass.exception.RecursoNaoEncontradoException;
import com.wavepass.model.Evento;
import com.wavepass.model.Setor;
import com.wavepass.repository.EventoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class EventoService {

    private final EventoRepository eventoRepository;
    private final Clock clock;

    public Evento cadastrar(EventoEntradaDTO dto) {
        Evento evento = Evento.builder()
                .nome(dto.nome())
                .artistas(dto.artistas())
                .genero(dto.genero())
                .categoria(dto.categoria())
                .faixaEtaria(dto.faixaEtaria())
                .dataHora(dto.dataHora())
                .local(dto.local())
                .precoMinimo(dto.precoMinimo())
                .corTema(dto.corTema())
                .imagemBanner(dto.imagemBanner())
                .imagemCard(dto.imagemCard())
                .descricaoCurta(dto.descricaoCurta())
                .descricaoCompleta(dto.descricaoCompleta())
                .setores(new ArrayList<>())
                .build();

        if (dto.setores() != null && !dto.setores().isEmpty()) {
            List<Setor> setores = dto.setores().stream().map(setorDto -> {
                Setor setor = new Setor();
                setor.setTipo(setorDto.tipo());
                setor.setPreco(setorDto.preco());
                setor.setEvento(evento);
                return setor;
            }).toList();

            evento.setSetores(setores);
        }

        return eventoRepository.save(evento);
    }

    public List<Evento> listarTodos() {
        return eventoRepository.findAll();
    }

    public List<Evento> listarProximosEventos() {
        return eventoRepository.findByDataHoraAfterOrderByDataHoraAsc(LocalDateTime.now(clock));
    }

    public List<Evento> buscarEventos(String categoria, LocalDate dataInicial, LocalDate dataFinal,
                                     Boolean nesteFimDeSemana, BigDecimal precoMaximo,
                                     Boolean incluirPrecoMaximo) {
        LocalDateTime agora = LocalDateTime.now(clock);
        LocalDateTime inicio = agora;
        LocalDateTime fim = null;

        if (dataInicial != null && dataInicial.atStartOfDay().isAfter(inicio)) {
            inicio = dataInicial.atStartOfDay();
        }
        if (dataFinal != null) {
            fim = dataFinal.plusDays(1).atStartOfDay();
        }
        if (Boolean.TRUE.equals(nesteFimDeSemana)) {
            LocalDate sabado = agora.toLocalDate()
                    .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).plusDays(5);
            if (sabado.atStartOfDay().isAfter(inicio)) {
                inicio = sabado.atStartOfDay();
            }
            LocalDateTime fimDeSemana = sabado.plusDays(2).atStartOfDay();
            if (fim == null || fimDeSemana.isBefore(fim)) {
                fim = fimDeSemana;
            }
        }

        LocalDateTime inicioConsulta = inicio;
        LocalDateTime fimConsulta = fim;
        Specification<Evento> filtro = (root, query, cb) -> {
            var criterios = new ArrayList<jakarta.persistence.criteria.Predicate>();
            criterios.add(cb.greaterThanOrEqualTo(root.get("dataHora"), inicioConsulta));
            if (fimConsulta != null) {
                criterios.add(cb.lessThan(root.get("dataHora"), fimConsulta));
            }
            if (categoria != null && !categoria.isBlank()) {
                criterios.add(cb.equal(cb.lower(cb.trim(root.get("categoria"))),
                        categoria.strip().toLowerCase(Locale.ROOT)));
            }
            if (precoMaximo != null) {
                criterios.add(Boolean.TRUE.equals(incluirPrecoMaximo)
                        ? cb.lessThanOrEqualTo(root.get("precoMinimo"), precoMaximo)
                        : cb.lessThan(root.get("precoMinimo"), precoMaximo));
            }
            return cb.and(criterios.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
        return eventoRepository.findAll(filtro, Sort.by("dataHora").ascending().and(Sort.by("id")));
    }

    public Evento buscarPorId(Long id) {
        return eventoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Evento não encontrado com o ID: " + id));
    }

    public Evento atualizar(Long id, EventoEntradaDTO dto) {
        Evento eventoExistente = buscarPorId(id);

        Evento eventoAtualizado = Evento.builder()
                .id(eventoExistente.getId())
                .nome(dto.nome())
                .artistas(dto.artistas())
                .genero(dto.genero())
                .categoria(dto.categoria())
                .faixaEtaria(dto.faixaEtaria())
                .dataHora(dto.dataHora())
                .local(dto.local())
                .precoMinimo(dto.precoMinimo())
                .corTema(dto.corTema())
                .imagemBanner(dto.imagemBanner())
                .imagemCard(dto.imagemCard())
                .descricaoCurta(dto.descricaoCurta())
                .descricaoCompleta(dto.descricaoCompleta())
                .setores(new ArrayList<>())
                .build();

        if (dto.setores() != null && !dto.setores().isEmpty()) {
            List<Setor> novosSetores = dto.setores().stream().map(setorDto -> {
                Setor setor = new Setor();
                setor.setTipo(setorDto.tipo());
                setor.setPreco(setorDto.preco());
                setor.setEvento(eventoAtualizado);
                return setor;
            }).toList();

            eventoAtualizado.setSetores(novosSetores);
        }

        return eventoRepository.save(eventoAtualizado);
    }

    public void deletar(Long id) {
        Evento evento = buscarPorId(id);
        eventoRepository.delete(evento);
    }
}
