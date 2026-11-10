package com.wavepass.controller;

import com.wavepass.dto.EventoEntradaDTO;
import com.wavepass.model.Evento;
import com.wavepass.service.EventoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/evento")
@RequiredArgsConstructor
public class EventoController {

    private final EventoService eventoService;

    @PostMapping
    public ResponseEntity<Evento> cadastrar(@RequestBody EventoEntradaDTO dto) {
        Evento novoEvento = eventoService.cadastrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoEvento);
    }

    @GetMapping
    public ResponseEntity<List<Evento>> listarTodos() {
        return ResponseEntity.ok(eventoService.listarTodos());
    }

    @GetMapping("/proximos")
    public ResponseEntity<List<Evento>> listarProximosEventos() {
        return ResponseEntity.ok(eventoService.listarProximosEventos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Evento> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(eventoService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Evento> atualizar(@PathVariable Long id, @RequestBody EventoEntradaDTO dto) {
        Evento eventoAtualizado = eventoService.atualizar(id, dto);
        return ResponseEntity.ok(eventoAtualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        eventoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}