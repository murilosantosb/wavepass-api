package com.wavepass.repository;

import com.wavepass.model.PerfilUsuario;
import com.wavepass.model.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    @DisplayName("Deve realizar o fluxo completo de CRUD do Usuario")
    void fluxoCompletoCrud() {
        Usuario usuario = new Usuario();
        usuario.setNome("Luiza Oliveira");
        usuario.setEmail("luiza.teste@email.com");
        usuario.setCpf("99988877766");
        usuario.setSenha("123456");
        usuario.setTelefone("11999999999");
        usuario.setDataNascimento(LocalDate.of(2006, 3, 21));
        usuario.setPerfil(PerfilUsuario.CLIENTE);

        Usuario usuarioSalvo = usuarioRepository.save(usuario);

        assertThat(usuarioSalvo.getId()).isNotNull();

        Optional<Usuario> usuarioBuscado = usuarioRepository.findById(usuarioSalvo.getId());
        assertThat(usuarioBuscado).isPresent();
        assertThat(usuarioBuscado.get().getEmail()).isEqualTo("luiza.teste@email.com");

        usuarioSalvo.setNome("Luiza Oliveira Atualizada");
        Usuario usuarioAtualizado = usuarioRepository.save(usuarioSalvo);
        assertThat(usuarioAtualizado.getNome()).isEqualTo("Luiza Oliveira Atualizada");

        usuarioRepository.deleteById(usuarioSalvo.getId());
        Optional<Usuario> usuarioAposDeletar = usuarioRepository.findById(usuarioSalvo.getId());
        assertThat(usuarioAposDeletar).isEmpty();
    }
}