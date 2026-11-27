package com.wavepass;

import com.jayway.jsonpath.JsonPath;
import com.sun.net.httpserver.HttpServer;
import com.wavepass.service.ChatService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:groqchat;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "api.security.token.secret=local-groq-test-secret",
        "spring.ai.openai.api-key=local-groq-test-key"
})
class GroqChatIntegrationTest {

    private static final List<String> requisicoes = new CopyOnWriteArrayList<>();
    private static HttpServer servidor;

    @Autowired
    private ChatService chatService;

    @DynamicPropertySource
    static void configurarProvedor(DynamicPropertyRegistry registry) throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.createContext("/v1/chat/completions", exchange -> {
            requisicoes.add(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            String mensagem = requisicoes.size() == 1 ? """
                    {"role":"assistant","content":null,
                     "reasoning_content":"Preciso consultar os eventos cadastrados.",
                     "tool_calls":[{"id":"consulta-eventos","type":"function",
                     "function":{"name":"listarProximosEventos","arguments":"{}"}}]}
                    """ : """
                    {"role":"assistant","content":"Nenhum próximo evento cadastrado."}
                    """;
            String resposta = """
                    {"id":"chat-teste","object":"chat.completion","created":1795440000,
                     "model":"openai/gpt-oss-20b","choices":[{"index":0,"message":%s,
                     "finish_reason":"%s"}],
                     "usage":{"prompt_tokens":10,"completion_tokens":5,"total_tokens":15}}
                    """.formatted(mensagem, requisicoes.size() == 1 ? "tool_calls" : "stop");
            byte[] corpo = resposta.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, corpo.length);
            try (var output = exchange.getResponseBody()) {
                output.write(corpo);
            }
        });
        servidor.start();
        registry.add("spring.ai.openai.base-url",
                () -> "http://127.0.0.1:" + servidor.getAddress().getPort() + "/v1");
    }

    @AfterAll
    static void encerrarProvedor() {
        if (servidor != null) {
            servidor.stop(0);
        }
    }

    @Test
    void executaFerramentaSemReenviarReasoningContentAoProvedor() {
        assertThat(chatService.responderPergunta("Quais eventos disponíveis?"))
                .isEqualTo("Nenhum próximo evento cadastrado.");

        assertThat(requisicoes).hasSize(2);
        String segundaRequisicao = requisicoes.get(1);
        assertThat(segundaRequisicao).doesNotContain("reasoning_content");
        assertThat(JsonPath.<List<String>>read(segundaRequisicao, "$.messages[*].role"))
                .containsExactly("system", "system", "user", "assistant", "tool");
        assertThat(JsonPath.<String>read(segundaRequisicao, "$.messages[3].tool_calls[0].function.name"))
                .isEqualTo("listarProximosEventos");
        assertThat(JsonPath.<String>read(segundaRequisicao, "$.messages[4].content"))
                .isEqualTo("[]");
    }
}
