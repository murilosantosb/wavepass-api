package com.wavepass;

import com.jayway.jsonpath.JsonPath;
import com.wavepass.model.Evento;
import com.wavepass.repository.EventoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:chat;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "api.security.token.secret=local-chat-test-secret",
        "spring.ai.openai.api-key=local-chat-test-key"
})
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class ChatIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private EventoRepository eventoRepository;

    @MockitoBean
    private ChatModel chatModel;

    @BeforeEach
    void configurar() {
        eventoRepository.deleteAll();
        when(chatModel.getOptions()).thenReturn(ToolCallingChatOptions.builder().build());
        when(chatModel.getDefaultOptions()).thenReturn(ToolCallingChatOptions.builder().build());
    }

    @Test
    void consultaFerramentaEEnviaSomenteEventosFuturosAoModelo() throws Exception {
        eventoRepository.save(evento("Show passado", LocalDateTime.now().minusDays(1)));
        Evento proximo = eventoRepository.save(evento("Show futuro", LocalDateTime.now().plusDays(1)));

        when(chatModel.call(any(Prompt.class))).thenReturn(chamadaFerramenta()).thenAnswer(invocacao -> {
            String resultado = resultadoFerramenta(invocacao.getArgument(0));
            List<?> eventos = JsonPath.read(resultado, "$");
            assertThat(eventos).hasSize(1);
            assertThat(JsonPath.<String>read(resultado, "$[0].nome")).isEqualTo("Show futuro");
            assertThat(JsonPath.<Number>read(resultado, "$[0].id").longValue()).isEqualTo(proximo.getId());
            assertThat(JsonPath.<String>read(resultado, "$[0].artistas")).isEqualTo("Artista teste");
            assertThat(JsonPath.<String>read(resultado, "$[0].local")).isEqualTo("Local teste");
            assertThat(JsonPath.<Number>read(resultado, "$[0].precoMinimo").intValue()).isEqualTo(80);
            assertThat(resultado).doesNotContain("setores", "descricaoCompleta", "Show passado");
            return resposta("O Show futuro está cadastrado no WavePass.");
        });

        mvc.perform(post("/api/wavepass/chat/mensagens")
                        .contentType("application/json")
                        .content("{\"pergunta\":\"Quais são os próximos eventos?\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("O Show futuro está cadastrado no WavePass."));
    }

    @Test
    void ferramentaRetornaListaVaziaQuandoNaoHaEventos() throws Exception {
        when(chatModel.call(any(Prompt.class))).thenReturn(chamadaFerramenta()).thenAnswer(invocacao -> {
            assertThat(resultadoFerramenta(invocacao.getArgument(0))).isEqualTo("[]");
            return resposta("Não foram encontrados próximos eventos cadastrados no WavePass.");
        });

        mvc.perform(post("/api/wavepass/chat/mensagens")
                        .contentType("application/json")
                        .content("{\"pergunta\":\"Tem algum show?\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Não foram encontrados próximos eventos cadastrados no WavePass."));
    }

    @Test
    void rejeitaPerguntasAusentesEmBrancoOuMuitoLongasSemChamarModelo() throws Exception {
        for (String corpo : List.of("{}", "{\"pergunta\":\"   \"}",
                "{\"pergunta\":\"" + "a".repeat(2001) + "\"}")) {
            mvc.perform(post("/api/wavepass/chat/mensagens")
                            .contentType("application/json").content(corpo))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.erros[0].campo").value("pergunta"));
        }
        verify(chatModel, never()).call(any(Prompt.class));
    }

    private Evento evento(String nome, LocalDateTime dataHora) {
        return Evento.builder()
                .nome(nome).artistas("Artista teste").genero("Rock").categoria("Show")
                .dataHora(dataHora).local("Local teste").precoMinimo(new BigDecimal("80.00"))
                .faixaEtaria(16).descricaoCompleta("Conteúdo fora do DTO da IA").build();
    }

    private ChatResponse chamadaFerramenta() {
        return new ChatResponse(List.of(new Generation(AssistantMessage.builder()
                .content("")
                .toolCalls(List.of(new AssistantMessage.ToolCall(
                        "consulta-eventos", "function", "listarProximosEventos", "{}")))
                .build())));
    }

    private ChatResponse resposta(String conteudo) {
        return new ChatResponse(List.of(new Generation(new AssistantMessage(conteudo))));
    }

    private String resultadoFerramenta(Prompt prompt) {
        return prompt.getInstructions().stream()
                .filter(ToolResponseMessage.class::isInstance)
                .map(ToolResponseMessage.class::cast)
                .flatMap(mensagem -> mensagem.getResponses().stream())
                .filter(resposta -> resposta.name().equals("listarProximosEventos"))
                .findFirst().orElseThrow().responseData();
    }
}
