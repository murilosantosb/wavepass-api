package com.wavepass.controller;

import com.wavepass.dto.LoginUsuarioDTO;
import com.wavepass.dto.UsuarioEntradaDTO;
import com.wavepass.dto.UsuarioSaidaDTO;
import com.wavepass.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wavepass/usuario")
@Tag(name = "Usuários", description = "Endpoints para gerenciamento e cadastro de usuários")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/register")
    @Operation(summary = "Cadastrar novo usuário", description = "Cria um novo perfil de cliente no sistema.")
    public ResponseEntity<UsuarioSaidaDTO> registrar(@RequestBody @Valid UsuarioEntradaDTO dto) {
        UsuarioSaidaDTO novoUsuario = usuarioService.cadastrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoUsuario);
    }

    @PostMapping("/login")
    @Operation(summary = "Login usuário", description = "Realizar Login e autenticação do usuário")
    public ResponseEntity login(@RequestBody LoginUsuarioDTO dto) {

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                dto.email(),
                dto.senha()
        );

        return ResponseEntity.ok().build();

    }

    @GetMapping
    @Operation(summary = "Listar todos os usuários", description = "Retorna a lista de usuários sem dados sensíveis.")
    public ResponseEntity<List<UsuarioSaidaDTO>> listarTodos() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuário por ID")
    public ResponseEntity<UsuarioSaidaDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar usuário existente")
    public ResponseEntity<UsuarioSaidaDTO> atualizar(@PathVariable Long id, @RequestBody @Valid UsuarioEntradaDTO dto) {
        UsuarioSaidaDTO usuarioAtualizado = usuarioService.atualizar(id, dto);
        return ResponseEntity.ok(usuarioAtualizado);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover usuário")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        usuarioService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}