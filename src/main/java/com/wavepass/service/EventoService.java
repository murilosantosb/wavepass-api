package com.wavepass.service;

import com.wavepass.dto.EventoEntradaDTO;
import com.wavepass.dto.EventoFiltroDTO;
import com.wavepass.exception.RecursoNaoEncontradoException;
import com.wavepass.model.Evento;
import com.wavepass.model.Setor;
import com.wavepass.repository.EventoRepository;
import com.wavepass.specification.EventoSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventoService {

    private final EventoRepository eventoRepository;

    public Evento cadastrar(EventoEntradaDTO dto) {
        Evento evento = Evento.builder()
                .nome(dto.nome())
                .artistas(dto.artistas())
                .genero(dto.genero())
                .categoria(dto.categoria())
                .faixaEtaria(dto.faixaEtaria())
                .local(dto.local())
                .cidade(dto.cidade())
                .estado(dto.estado())
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

    @Transactional(readOnly = true)
    public Page<Evento> listarTodos(EventoFiltroDTO filtro, Pageable pageable) {
        return eventoRepository.findAll(EventoSpecification.comFiltros(filtro), pageable);
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
                .local(dto.local())
                .cidade(dto.cidade())
                .estado(dto.estado())
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