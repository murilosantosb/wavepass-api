package com.wavepass.controller;

import com.wavepass.dto.EventoEntradaDTO;
import com.wavepass.dto.EventoFiltroDTO;
import com.wavepass.model.Evento;
import com.wavepass.service.EventoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wavepass/evento")
@Tag(name = "Eventos", description = "Endpoints para gestão e consulta de eventos")
public class EventoController {

    private final EventoService eventoService;

    @PostMapping
    @Operation(summary = "Cadastrar novo evento com setores e atrações")
    public ResponseEntity<Evento> cadastrar(@RequestBody @Valid EventoEntradaDTO dto) {
        Evento novoEvento = eventoService.cadastrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoEvento);
    }

    @GetMapping
    @Operation(summary = "Listar todos os eventos")
    public ResponseEntity<Page<Evento>> listarTodos(
            @ParameterObject EventoFiltroDTO filtro,
            @ParameterObject @PageableDefault(size = 12, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(eventoService.listarTodos(filtro, pageable));
    }

    @GetMapping("/meus-eventos")
    @Operation(summary = "Listar os eventos cadastrados pelo administrador logado")
    public ResponseEntity<Page<Evento>> listarMeusEventos(
            @ParameterObject @PageableDefault(size = 12, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(eventoService.listarMeusEventos(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar detalhes do evento por ID")
    public ResponseEntity<Evento> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(eventoService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar dados do evento")
    public ResponseEntity<Evento> atualizar(@PathVariable Long id, @RequestBody @Valid EventoEntradaDTO dto) {
        Evento eventoAtualizado = eventoService.atualizar(id, dto);
        return ResponseEntity.ok(eventoAtualizado);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar evento")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        eventoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}