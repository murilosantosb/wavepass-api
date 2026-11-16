package com.wavepass.service;

import com.wavepass.dto.UsuarioEntradaDTO;
import com.wavepass.dto.UsuarioSaidaDTO;
import com.wavepass.exception.RecursoNaoEncontradoException;
import com.wavepass.exception.RegraNegocioException;
import com.wavepass.model.PerfilUsuario;
import com.wavepass.model.Usuario;
import com.wavepass.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioSaidaDTO cadastrar(UsuarioEntradaDTO dto) {
        if (usuarioRepository.existsByEmail(dto.email())) {
            throw new RegraNegocioException("E-mail já cadastrado no sistema.");
        }
        if (usuarioRepository.existsByCpf(dto.cpf())) {
            throw new RegraNegocioException("CPF já cadastrado no sistema.");
        }

        Usuario usuario = Usuario.builder()
                .nome(dto.nome())
                .telefone(dto.telefone())
                .cpf(dto.cpf())
                .dataNascimento(dto.dataNascimento())
                .email(dto.email())
                .senha(passwordEncoder.encode(dto.senha()))
                .perfil(dto.perfil() != null ? dto.perfil() : PerfilUsuario.CLIENTE)
                .build();

        Usuario salvo = usuarioRepository.save(usuario);
        return UsuarioSaidaDTO.doModelo(salvo);
    }

    public List<UsuarioSaidaDTO> listarTodos() {
        return usuarioRepository.findAll()
                .stream()
                .map(UsuarioSaidaDTO::doModelo)
                .toList();
    }

    public UsuarioSaidaDTO buscarPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado com o ID: " + id));
        return UsuarioSaidaDTO.doModelo(usuario);
    }

    public UsuarioSaidaDTO atualizar(Long id, UsuarioEntradaDTO dto) {
        Usuario usuarioExistente = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado com o ID: " + id));

        Usuario usuarioAtualizado = Usuario.builder()
                .id(usuarioExistente.getId())
                .nome(dto.nome() != null ? dto.nome() : usuarioExistente.getNome())
                .telefone(dto.telefone() != null ? dto.telefone() : usuarioExistente.getTelefone())
                .cpf(dto.cpf() != null ? dto.cpf() : usuarioExistente.getCpf()) // <--- Altere para usar o dto.cpf()
                .dataNascimento(dto.dataNascimento() != null ? dto.dataNascimento() : usuarioExistente.getDataNascimento())
                .email(dto.email() != null ? dto.email() : usuarioExistente.getEmail())
                .senha(dto.senha() != null ? dto.senha() : usuarioExistente.getSenha())
                .perfil(dto.perfil() != null ? dto.perfil() : usuarioExistente.getPerfil())
                .dataCriacao(usuarioExistente.getDataCriacao())
                .build();

        Usuario salvo = usuarioRepository.save(usuarioAtualizado);
        return UsuarioSaidaDTO.doModelo(salvo);
    }

    public void deletar(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado com o ID: " + id));
        usuarioRepository.delete(usuario);
    }
}
