package com.wavepass.tool;

import com.wavepass.dto.EventoAiDTO;
import com.wavepass.service.EventoService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EventoTools {

    private final EventoService eventoService;

    public EventoTools(EventoService eventoService) {
        this.eventoService = eventoService;
    }

    @Tool(description = "Consulta os próximos eventos cadastrados no WavePass, ordenados por data. "
            + "Retorna nome, artistas, data e hora, local, preço mínimo e classificação etária. "
            + "Use para responder sobre eventos e shows disponíveis na plataforma.")
    public List<EventoAiDTO> listarProximosEventos() {
        return eventoService.listarProximosEventos().stream()
                .map(evento -> new EventoAiDTO(
                        evento.getId(),
                        evento.getNome(),
                        evento.getArtistas(),
                        evento.getDataHora(),
                        evento.getLocal(),
                        evento.getPrecoMinimo(),
                        evento.getFaixaEtaria()))
                .toList();
    }
}
