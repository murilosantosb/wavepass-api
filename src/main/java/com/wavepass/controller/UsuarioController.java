package com.wavepass.controller;

import com.wavepass.dto.*;
import com.wavepass.model.Usuario;
import com.wavepass.service.TokenService;
import com.wavepass.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wavepass/usuario")
@Tag(name = "Usuários", description = "Endpoints para gerenciamento e cadastro de usuários")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/registerClient")
    @SecurityRequirements
    @Operation(summary = "Cadastrar novo cliente", description = "Endpoint público para cadastro de clientes no sistema.")
    public ResponseEntity<UsuarioSaidaDTO> registrarCliente(@RequestBody @Valid ClienteEntradaDTO dto) {
        UsuarioSaidaDTO novoUsuario = usuarioService.cadastrarCliente(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoUsuario);
    }

    @PostMapping("/registerAdmin")
    @Operation(summary = "Cadastrar empresa/administrador", description = "Endpoint restrito à equipe Wavepass para cadastro de organizadores.")
    public ResponseEntity<UsuarioSaidaDTO> registrarAdmin(@RequestBody @Valid AdminEntradaDTO dto) {
        UsuarioSaidaDTO novoUsuario = usuarioService.cadastrarAdmin(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoUsuario);
    }

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Login usuário", description = "Realizar login e autenticação do usuário")
    public ResponseEntity<TokenDTO> login(@RequestBody @Valid LoginUsuarioDTO dto) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                dto.email(),
                dto.senha()
        );

        var authentication = authenticationManager.authenticate(token);
        var usuario = (Usuario) authentication.getPrincipal();

        return ResponseEntity.ok(new TokenDTO(
                tokenService.gerarToken(usuario),
                usuario.getId(),
                usuario.getNome(),
                usuario.getPerfil()
        ));
    }

    @GetMapping
    @Operation(summary = "Listar todos os usuários", description = "Retorna a lista de usuários (restrito à equipe Wavepass).")
    public ResponseEntity<List<UsuarioSaidaDTO>> listarTodos() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuário por ID")
    public ResponseEntity<UsuarioSaidaDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    @PutMapping("/client/{id}")
    @Operation(summary = "Atualizar cliente existente")
    public ResponseEntity<UsuarioSaidaDTO> atualizarCliente(@PathVariable Long id,
                                                            @RequestBody @Valid ClienteEntradaDTO dto) {
        UsuarioSaidaDTO usuarioAtualizado = usuarioService.atualizarCliente(id, dto);
        return ResponseEntity.ok(usuarioAtualizado);
    }

    @PutMapping("/admin/{id}")
    @Operation(summary = "Atualizar empresa/administrador existente")
    public ResponseEntity<UsuarioSaidaDTO> atualizarAdmin(@PathVariable Long id,
                                                          @RequestBody @Valid AdminEntradaDTO dto) {
        UsuarioSaidaDTO usuarioAtualizado = usuarioService.atualizarAdmin(id, dto);
        return ResponseEntity.ok(usuarioAtualizado);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover usuário")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        usuarioService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}