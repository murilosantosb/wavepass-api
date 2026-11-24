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
        if (dto.cnpj() != null && !dto.cnpj().isBlank() && usuarioRepository.existsByCnpj(dto.cnpj())) {
            throw new RegraNegocioException("CNPJ já cadastrado no sistema.");
        }

        Usuario usuario = new Usuario();
        preencherDados(usuario, dto);

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

        if (dto.email() != null && !dto.email().equals(usuarioExistente.getEmail())
                && usuarioRepository.existsByEmail(dto.email())) {
            throw new RegraNegocioException("E-mail já cadastrado no sistema.");
        }
        if (dto.cpf() != null && !dto.cpf().isBlank() && !dto.cpf().equals(usuarioExistente.getCpf())
                && usuarioRepository.existsByCpf(dto.cpf())) {
            throw new RegraNegocioException("CPF já cadastrado no sistema.");
        }
        if (dto.cnpj() != null && !dto.cnpj().isBlank() && !dto.cnpj().equals(usuarioExistente.getCnpj())
                && usuarioRepository.existsByCnpj(dto.cnpj())) {
            throw new RegraNegocioException("CNPJ já cadastrado no sistema.");
        }

        preencherDados(usuarioExistente, dto);

        Usuario salvo = usuarioRepository.save(usuarioExistente);
        return UsuarioSaidaDTO.doModelo(salvo);

    }

    public void deletar(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado com o ID: " + id));
        usuarioRepository.delete(usuario);
    }

    private void preencherDados(Usuario usuario, UsuarioEntradaDTO dto) {
        PerfilUsuario perfil = dto.perfil() != null
                ? dto.perfil()
                : (usuario.getPerfil() != null ? usuario.getPerfil() : PerfilUsuario.CLIENTE);

        if (dto.nome() != null) {
            usuario.setNome(dto.nome());
        }
        if (dto.email() != null) {
            usuario.setEmail(dto.email());
        }
        if (dto.telefone() != null) {
            usuario.setTelefone(dto.telefone());
        }
        if (dto.cpf() != null) {
            usuario.setCpf(dto.cpf());
        }
        if (dto.dataNascimento() != null) {
            usuario.setDataNascimento(dto.dataNascimento());
        }
        if (dto.cnpj() != null) {
            usuario.setCnpj(dto.cnpj());
        }
        if (dto.nomeEmpresa() != null) {
            usuario.setNomeEmpresa(dto.nomeEmpresa());
        }

        usuario.setPerfil(perfil);

        if (dto.senha() != null && !dto.senha().isBlank()) {
            usuario.setSenha(passwordEncoder.encode(dto.senha()));
        }

        validarCamposPorPerfil(usuario, perfil);
    }

    private void validarCamposPorPerfil(Usuario usuario, PerfilUsuario perfil) {
        if (perfil == PerfilUsuario.CLIENTE) {
            if (usuario.getCpf() == null || usuario.getCpf().isBlank()) {
                throw new RegraNegocioException("O CPF é obrigatório para cadastro de cliente.");
            }
            if (usuario.getDataNascimento() == null) {
                throw new RegraNegocioException("A data de nascimento é obrigatória para cadastro de cliente.");
            }
            if (usuario.getTelefone() == null || usuario.getTelefone().isBlank()) {
                throw new RegraNegocioException("O telefone é obrigatório para cadastro de cliente.");
            }
        } else if (perfil == PerfilUsuario.ADMIN) {
            if (usuario.getCnpj() == null || usuario.getCnpj().isBlank()) {
                throw new RegraNegocioException("O CNPJ da empresa é obrigatório para administrador.");
            }
            if (usuario.getNomeEmpresa() == null || usuario.getNomeEmpresa().isBlank()) {
                throw new RegraNegocioException("O nome da empresa contratante é obrigatório para administrador.");
            }
        }
    }
}