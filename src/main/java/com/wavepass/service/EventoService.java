
package com.wavepass.service;

import com.wavepass.dto.EventoEntradaDTO;
import com.wavepass.dto.EventoFiltroDTO;
import com.wavepass.dto.EventoAiDTO;
import com.wavepass.exception.RecursoNaoEncontradoException;
import com.wavepass.model.*;
import com.wavepass.repository.EventoRepository;
import com.wavepass.repository.UsuarioRepository;
import com.wavepass.specification.EventoSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class EventoService {

    private final EventoRepository eventoRepository;
    private final Clock clock;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public Evento cadastrar(EventoEntradaDTO dto) {
        Evento evento = new Evento();
        evento.setCriadoPor(getUsuarioLogado());
        preencherDados(evento, dto);

        return eventoRepository.save(evento);
    }

    @Transactional
    public Evento atualizar(Long id, EventoEntradaDTO dto) {
        Evento eventoExistente = buscarPorId(id);
        validarPermissaoSobreEvento(eventoExistente);
        preencherDados(eventoExistente, dto);

        return eventoRepository.save(eventoExistente);
    }

    @Transactional(readOnly = true)
    public Page<Evento> listarTodos(EventoFiltroDTO filtro, Pageable pageable) {
        return eventoRepository.findAll(
                EventoSpecification.comFiltros(filtro), pageable);
    }

    @Transactional(readOnly = true)
    public List<EventoAiDTO> listarProximosEventos() {
        return buscarEventos(null, null, null, null, null, null);
    }

    @Transactional(readOnly = true)
    public List<EventoAiDTO> buscarEventos(
            String categoria,
            LocalDate dataInicial,
            LocalDate dataFinal,
            Boolean nesteFimDeSemana,
            BigDecimal precoMaximo,
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
                    .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    .plusDays(5);

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
        CategoriaEvento categoriaConsulta;
        try {
            categoriaConsulta = categoria == null || categoria.isBlank()
                    ? null : CategoriaEvento.porDescricao(categoria);
        } catch (IllegalArgumentException exception) {
            return List.of();
        }

        Specification<Evento> filtro = (root, query, cb) -> {
            var criterios = new ArrayList<jakarta.persistence.criteria.Predicate>();
            var sessoes = root.join("sessoes");
            query.distinct(true);

            criterios.add(cb.greaterThanOrEqualTo(
                    sessoes.get("dataHora"), inicioConsulta));

            if (fimConsulta != null) {
                criterios.add(cb.lessThan(
                        sessoes.get("dataHora"), fimConsulta));
            }

            if (categoriaConsulta != null) {
                criterios.add(cb.equal(root.get("categoria"), categoriaConsulta));
            }

            if (precoMaximo != null) {
                criterios.add(Boolean.TRUE.equals(incluirPrecoMaximo)
                        ? cb.lessThanOrEqualTo(root.get("precoMinimo"), precoMaximo)
                        : cb.lessThan(root.get("precoMinimo"), precoMaximo));
            }

            return cb.and(
                    criterios.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };

        return eventoRepository.findAll(filtro).stream()
                .map(evento -> new EventoAiDTO(
                        evento.getId(), evento.getNome(), evento.getArtistas(), evento.getCategoria(),
                        evento.getSessoes().stream().map(SessaoEvento::getDataHora)
                                .filter(data -> !data.isBefore(inicioConsulta))
                                .filter(data -> fimConsulta == null || data.isBefore(fimConsulta))
                                .min(Comparator.naturalOrder()).orElseThrow(),
                        evento.getLocal(), evento.getPrecoMinimo(), evento.getFaixaEtaria()))
                .sorted(Comparator.comparing(EventoAiDTO::dataHora).thenComparing(EventoAiDTO::id))
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<Evento> listarMeusEventos(Pageable pageable) {
        Usuario usuarioLogado = getUsuarioLogado();

        return eventoRepository.findByCriadoPorId(
                usuarioLogado.getId(), pageable);
    }

    @Transactional(readOnly = true)
    public Evento buscarPorId(Long id) {
        return eventoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Evento não encontrado com o ID: " + id));
    }

    @Transactional
    public void deletar(Long id) {
        Evento evento = buscarPorId(id);
        validarPermissaoSobreEvento(evento);
        eventoRepository.delete(evento);
    }

    private void preencherDados(Evento evento, EventoEntradaDTO dto) {
        evento.setNome(dto.nome());
        evento.setArtistas(dto.artistas());
        evento.setGenero(dto.genero());
        evento.setCategoria(dto.categoria());
        evento.setFaixaEtaria(dto.faixaEtaria());
        evento.setLocal(dto.local());
        evento.setCidade(dto.cidade());
        evento.setEstado(dto.estado());
        evento.setPrecoMinimo(dto.precoMinimo());
        evento.setCorTema(dto.corTema());
        evento.setImagemBanner(dto.imagemBanner());
        evento.setImagemCard(dto.imagemCard());
        evento.setDescricaoCurta(dto.descricaoCurta());
        evento.setDescricaoCompleta(dto.descricaoCompleta());

        evento.getSetores().clear();

        if (dto.setores() != null) {
            dto.setores().forEach(setorEntradaDTO -> {
                Setor setor = new Setor();
                setor.setTipo(setorEntradaDTO.tipo());
                setor.setPreco(setorEntradaDTO.preco());
                setor.setEvento(evento);
                evento.getSetores().add(setor);
            });
        }

        evento.getSessoes().clear();

        if (dto.datas() != null) {
            dto.datas().forEach(dataHora -> {
                SessaoEvento sessao = SessaoEvento.builder()
                        .dataHora(dataHora)
                        .build();

                evento.getSessoes().add(sessao);
            });
        }
    }

    private void validarPermissaoSobreEvento(Evento evento) {
        Usuario usuarioLogado = getUsuarioLogado();

        if (usuarioLogado.getPerfil() == PerfilUsuario.ADMIN) {
            boolean eDono = evento.getCriadoPor() != null
                    && evento.getCriadoPor().getId().equals(usuarioLogado.getId());

            if (!eDono) {
                throw new AccessDeniedException(
                        "Você só pode alterar ou excluir seus eventos cadastrados.");
            }
        }
    }

    private Usuario getUsuarioLogado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) {
            throw new AccessDeniedException("Usuário não autenticado.");
        }

        if (auth.getPrincipal() instanceof Usuario usuario) {
            return usuario;
        }

        return usuarioRepository.findUsuarioByEmail(auth.getName())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Usuário logado não encontrado."));
    }
}
