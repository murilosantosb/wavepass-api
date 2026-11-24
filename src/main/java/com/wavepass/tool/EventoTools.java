package com.wavepass.tool;

import com.wavepass.dto.EventoAiDTO;
import com.wavepass.service.EventoService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
public class EventoTools {

    private final EventoService eventoService;

    public EventoTools(EventoService eventoService) {
        this.eventoService = eventoService;
    }

    @Tool(description = "Consulta os próximos eventos cadastrados no WavePass, ordenados por data. "
            + "Retorna nome, artistas, categoria, data e hora, local, preço mínimo e classificação etária. "
            + "Para consultas com categoria, período ou preço, prefira buscarEventos.")
    public List<EventoAiDTO> listarProximosEventos() {
        return eventoService.listarProximosEventos().stream()
                .map(evento -> new EventoAiDTO(
                        evento.getId(),
                        evento.getNome(),
                        evento.getArtistas(),
                        evento.getCategoria(),
                        evento.getDataHora(),
                        evento.getLocal(),
                        evento.getPrecoMinimo(),
                        evento.getFaixaEtaria()))
                .toList();
    }

    @Tool(description = "Busca eventos futuros no WavePass aplicando filtros no banco e ordenando por data. "
            + "Use para shows ou cinema neste fim de semana e consultas por preço. "
            + "Filtros omitidos não restringem a busca. O preço é o preço mínimo cadastrado, não de todos os ingressos.")
    public List<EventoAiDTO> buscarEventos(
            @ToolParam(required = false, description = "Categoria cadastrada. Use Show para shows e Cinema para cinema.")
            String categoria,
            @ToolParam(required = false, description = "Data inicial inclusiva no formato AAAA-MM-DD, no fuso America/Sao_Paulo.")
            LocalDate dataInicial,
            @ToolParam(required = false, description = "Data final inclusiva no formato AAAA-MM-DD, no fuso America/Sao_Paulo.")
            LocalDate dataFinal,
            @ToolParam(required = false, description = "true para este fim de semana: sábado e domingo da semana atual, apenas eventos ainda futuros.")
            Boolean nesteFimDeSemana,
            @ToolParam(required = false, description = "Preço máximo em reais. Para menos de 300 use 300; para gratuitos use 0 com incluirPrecoMaximo=true.")
            BigDecimal precoMaximo,
            @ToolParam(required = false, description = "true para até ou no máximo; false ou omitido para abaixo de ou menos de.")
            Boolean incluirPrecoMaximo) {
        return eventoService.buscarEventos(categoria, dataInicial, dataFinal, nesteFimDeSemana,
                        precoMaximo, incluirPrecoMaximo).stream()
                .map(evento -> new EventoAiDTO(evento.getId(), evento.getNome(), evento.getArtistas(),
                        evento.getCategoria(), evento.getDataHora(), evento.getLocal(),
                        evento.getPrecoMinimo(), evento.getFaixaEtaria()))
                .toList();
    }
}
