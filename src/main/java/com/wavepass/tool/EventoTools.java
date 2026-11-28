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
            + "Retorna nome, artistas, categoria, data e hora, local, cidade, estado, preço mínimo e classificação etária. "
            + "Para consultas com categoria, período, preço ou localização, prefira buscarEventos.")
    public List<EventoAiDTO> listarProximosEventos() {
        return eventoService.listarProximosEventos();
    }

    @Tool(description = "Busca eventos futuros no WavePass aplicando filtros no banco e ordenando por data. "
            + "Use para consultas por categoria, período, preço, cidade ou estado. "
            + "Filtros omitidos não restringem a busca. O preço é o preço mínimo cadastrado, não de todos os ingressos.")
    public List<EventoAiDTO> buscarEventos(
            @ToolParam(required = false, description = "Categoria cadastrada. Use SHOW para shows e FILMES para cinema.")
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
            Boolean incluirPrecoMaximo,
            @ToolParam(required = false, description = "Cidade ou estado solicitado. Aceita siglas como SP e RJ e nomes completos como São Paulo ou Rio de Janeiro, com ou sem acentos. Não preencha se não solicitado.")
            String localizacao) {
        return eventoService.buscarEventos(categoria, dataInicial, dataFinal, nesteFimDeSemana,
                precoMaximo, incluirPrecoMaximo, localizacao);
    }

    public List<EventoAiDTO> buscarEventos(String categoria, LocalDate dataInicial, LocalDate dataFinal,
                                          Boolean nesteFimDeSemana, BigDecimal precoMaximo,
                                          Boolean incluirPrecoMaximo) {
        return buscarEventos(categoria, dataInicial, dataFinal, nesteFimDeSemana,
                precoMaximo, incluirPrecoMaximo, null);
    }
}
