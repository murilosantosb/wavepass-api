package com.wavepass.controller;

import com.wavepass.dto.EventoEntradaDTO;
import com.wavepass.model.Evento;
import com.wavepass.service.EventoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/evento")
@RequiredArgsConstructor
@Tag(name = "Eventos", description = "Endpoints para gestão e consulta de eventos")
public class EventoController {

    private final EventoService eventoService;

    @PostMapping
    @Operation(summary = "Cadastrar novo evento com setores e atrações")
    public ResponseEntity<Evento> cadastrar(@RequestBody EventoEntradaDTO dto) {
        Evento novoEvento = eventoService.cadastrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoEvento);
    }

    @GetMapping
    @Operation(summary = "Listar todos os eventos")
    public ResponseEntity<List<Evento>> listarTodos() {
        return ResponseEntity.ok(eventoService.listarTodos());
    }

    @GetMapping("/proximos")
    @Operation(summary = "Listar próximos eventos", description = "Alimenta a Home do App Mobile ordenando pela data futura.")
    public ResponseEntity<List<Evento>> listarProximosEventos() {
        return ResponseEntity.ok(eventoService.listarProximosEventos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar detalhes do evento por ID")
    public ResponseEntity<Evento> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(eventoService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar dados do evento")
    public ResponseEntity<Evento> atualizar(@PathVariable Long id, @RequestBody EventoEntradaDTO dto) {
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