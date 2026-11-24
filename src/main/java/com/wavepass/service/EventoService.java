package com.wavepass.service;

import com.wavepass.dto.EventoEntradaDTO;
import com.wavepass.dto.EventoFiltroDTO;
import com.wavepass.exception.RecursoNaoEncontradoException;
import com.wavepass.model.*;
import com.wavepass.repository.EventoRepository;
import com.wavepass.specification.EventoSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.wavepass.repository.UsuarioRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
@RequiredArgsConstructor
public class EventoService {

    private final EventoRepository eventoRepository;
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
        return eventoRepository.findAll(EventoSpecification.comFiltros(filtro), pageable);
    }

    @Transactional(readOnly = true)
    public Page<Evento> listarMeusEventos(Pageable pageable) {
        Usuario usuarioLogado = getUsuarioLogado();
        return eventoRepository.findByCriadoPorId(usuarioLogado.getId(), pageable);
    }

    @Transactional(readOnly = true)
    public Evento buscarPorId(Long id) {
        return eventoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Evento não encontrado com o ID: " + id));
    }

    @Transactional
    public void deletar(Long id) {
        Evento evento = buscarPorId(id);
        validarPermissaoSobreEvento(evento);
        eventoRepository.delete(evento);
    }

    private void preencherDados(Evento evento, EventoEntradaDTO dto){
        evento.setNome(dto.nome());
        evento.setArtistas(dto.artistas());
        evento.setGenero(dto.genero());
        evento.setCategoria(dto.categoria());
        evento.setFaixaEtaria(dto.faixaEtaria());
        evento.setLocal(dto.local());
        evento.setCidade(dto.cidade());
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
        if(dto.datas() != null) {
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
                throw new AccessDeniedException("Você só pode alterar ou excluir seus eventos cadastrados.");
            }
        }
    }

    private Usuario getUsuarioLogado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new AccessDeniedException("Usuário não autenticado.");
        }
        if (auth.getPrincipal() instanceof Usuario usuario) {
            return usuario;
        }
        return usuarioRepository.findUsuarioByEmail(auth.getName())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário logado não encontrado."));

    }
}