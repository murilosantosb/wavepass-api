package com.wavepass.service;

import com.wavepass.dto.AdminEntradaDTO;
import com.wavepass.dto.ClienteEntradaDTO;
import com.wavepass.dto.UsuarioSaidaDTO;
import com.wavepass.exception.RecursoNaoEncontradoException;
import com.wavepass.exception.RegraNegocioException;
import com.wavepass.model.PerfilUsuario;
import com.wavepass.model.Usuario;
import com.wavepass.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UsuarioSaidaDTO cadastrarCliente(ClienteEntradaDTO dto) {
        validarEmailUnico(dto.email());

        if (dto.cpf() != null && !dto.cpf().isBlank() && usuarioRepository.existsByCpf(dto.cpf())) {
            throw new RegraNegocioException("CPF já cadastrado no sistema.");
        }

        Usuario cliente = Usuario.builder()
                .nome(dto.nome())
                .email(dto.email())
                .cpf(dto.cpf())
                .telefone(dto.telefone())
                .dataNascimento(dto.dataNascimento())
                .senha(passwordEncoder.encode(dto.senha()))
                .perfil(PerfilUsuario.CLIENTE)
                .build();

        return UsuarioSaidaDTO.doModelo(usuarioRepository.save(cliente));
    }

    @Transactional
    public UsuarioSaidaDTO cadastrarAdmin(AdminEntradaDTO dto){
        validarEmailUnico(dto.email());

        if (dto.cnpj() != null && !dto.cnpj().isBlank() && usuarioRepository.existsByCnpj(dto.cnpj())){
            throw new RegraNegocioException("CNPJ já cadastrado no sistema.");
        }

        Usuario admin = Usuario.builder()
                .nome(dto.nome())
                .nomeEmpresa(dto.nomeEmpresa())
                .email(dto.email())
                .cnpj(dto.cnpj())
                .telefone(dto.telefone())
                .senha(passwordEncoder.encode(dto.senha()))
                .perfil(PerfilUsuario.ADMIN)
                .build();

        return UsuarioSaidaDTO.doModelo(usuarioRepository.save(admin));
    }

    @Transactional(readOnly = true)
    public List<UsuarioSaidaDTO> listarTodos() {
        return usuarioRepository.findAll()
                .stream()
                .map(UsuarioSaidaDTO::doModelo)
                .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioSaidaDTO buscarPorId(Long id) {
        validarAcesso(id);
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado com o ID: " + id));
        return UsuarioSaidaDTO.doModelo(usuario);
    }

    @Transactional
    public UsuarioSaidaDTO atualizarCliente(Long id, ClienteEntradaDTO dto) {
        validarAcesso(id);
        Usuario usuarioExistente = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado com o ID: " + id));

        validarPerfil(usuarioExistente, PerfilUsuario.CLIENTE);

        if (dto.email() != null && !dto.email().equals(usuarioExistente.getEmail())
                && usuarioRepository.existsByEmail(dto.email())) {
            throw new RegraNegocioException("E-mail já cadastrado no sistema.");
        }
        if (dto.cpf() != null && !dto.cpf().isBlank() && !dto.cpf().equals(usuarioExistente.getCpf())
                && usuarioRepository.existsByCpf(dto.cpf())) {
            throw new RegraNegocioException("CPF já cadastrado no sistema.");
        }

        usuarioExistente.setNome(dto.nome());
        usuarioExistente.setEmail(dto.email());
        usuarioExistente.setCpf(dto.cpf());
        usuarioExistente.setTelefone(dto.telefone());
        usuarioExistente.setDataNascimento(dto.dataNascimento());

        if (dto.senha() != null && !dto.senha().isBlank()) {
           usuarioExistente.setSenha(passwordEncoder.encode(dto.senha()));
        }

        return UsuarioSaidaDTO.doModelo(usuarioRepository.save(usuarioExistente));

    }

    @Transactional
    public UsuarioSaidaDTO atualizarAdmin(Long id, AdminEntradaDTO dto) {
        if (getUsuarioLogado().getPerfil() != PerfilUsuario.WAVEPASS) {
            throw new AccessDeniedException("Acesso restrito à equipe Wavepass.");
        }
        Usuario usuarioExistente = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado com o ID: " + id));

        validarPerfil(usuarioExistente, PerfilUsuario.ADMIN);

        if (dto.email() != null && !dto.email().equals(usuarioExistente.getEmail())
                && usuarioRepository.existsByEmail(dto.email())) {
            throw new RegraNegocioException("E-mail já cadastrado no sistema.");
        }
        if (dto.cnpj() != null && !dto.cnpj().isBlank() && !dto.cnpj().equals(usuarioExistente.getCnpj())
                && usuarioRepository.existsByCnpj(dto.cnpj())) {
            throw new RegraNegocioException("CNPJ já cadastrado no sistema.");
        }

        usuarioExistente.setNome(dto.nome());
        usuarioExistente.setNomeEmpresa(dto.nomeEmpresa());
        usuarioExistente.setEmail(dto.email());
        usuarioExistente.setCnpj(dto.cnpj());
        usuarioExistente.setTelefone(dto.telefone());

        if (dto.senha() != null && !dto.senha().isBlank()) {
            usuarioExistente.setSenha(passwordEncoder.encode(dto.senha()));
        }

        return UsuarioSaidaDTO.doModelo(usuarioRepository.save(usuarioExistente));

    }

    @Transactional
    public void deletar(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado com o ID: " + id));
        usuarioRepository.delete(usuario);
    }

    private void validarAcesso(Long id) {
        Usuario usuario = getUsuarioLogado();
        if (usuario.getPerfil() != PerfilUsuario.WAVEPASS && !id.equals(usuario.getId())) {
            throw new AccessDeniedException("Você só pode acessar sua própria conta.");
        }
    }

    private void validarPerfil(Usuario usuario, PerfilUsuario perfil) {
        if (usuario.getPerfil() != perfil) {
            throw new RegraNegocioException("O perfil do usuário não corresponde a esta operação.");
        }
    }

    private Usuario getUsuarioLogado() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new AccessDeniedException("Usuário não autenticado.");
        }
        if (auth.getPrincipal() instanceof Usuario usuario) {
            return usuario;
        }
        return usuarioRepository.findUsuarioByEmail(auth.getName())
                .orElseThrow(() -> new AccessDeniedException("Usuário não autenticado."));
    }

    private void validarEmailUnico(String email) {
        if (usuarioRepository.existsByEmail(email)) {
            throw new RegraNegocioException("E-mail já cadastrado no sistema.");
        }

    }

}

