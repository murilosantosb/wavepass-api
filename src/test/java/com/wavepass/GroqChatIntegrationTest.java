package com.wavepass;

import com.jayway.jsonpath.JsonPath;
import com.sun.net.httpserver.HttpServer;
import com.wavepass.exception.AssistenteIndisponivelException;
import com.wavepass.exception.AssistenteLimiteExcedidoException;
import com.wavepass.service.ChatService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:groqchat;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "api.security.token.secret=local-groq-test-secret",
        "spring.ai.openai.api-key=local-groq-test-key",
        "spring.ai.openai.chat.timeout=1s"
})
class GroqChatIntegrationTest {

    private static final List<String> requisicoes = new CopyOnWriteArrayList<>();
    private static final AtomicInteger falhasRestantes = new AtomicInteger();
    private static final AtomicBoolean ferramentaSolicitada = new AtomicBoolean();
    private static final ExecutorService executor = Executors.newCachedThreadPool();
    private static volatile int codigoFalha;
    private static volatile long atraso;
    private static volatile boolean chamarFerramenta;
    private static HttpServer servidor;

    @Autowired
    private ChatService chatService;

    @DynamicPropertySource
    static void configurarProvedor(DynamicPropertyRegistry registry) throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.setExecutor(executor);
        servidor.createContext("/v1/chat/completions", exchange -> {
            requisicoes.add(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            long atrasoAtual = atraso;
            int status = 200;
            String resposta;
            if (falhasRestantes.getAndUpdate(valor -> Math.max(0, valor - 1)) > 0) {
                status = codigoFalha;
                exchange.getResponseHeaders().set("Retry-After", "0.01");
                resposta = "{\"error\":{\"message\":\"Falha simulada\",\"type\":\"api_error\"}}";
            } else {
                boolean consulta = chamarFerramenta && !ferramentaSolicitada.getAndSet(true);
                String mensagem = consulta ? """
                        {"role":"assistant","content":null,
                         "reasoning_content":"Preciso consultar os eventos cadastrados.",
                         "tool_calls":[{"id":"consulta-eventos","type":"function",
                         "function":{"name":"listarProximosEventos","arguments":"{}"}}]}
                        """ : """
                        {"role":"assistant","content":"Nenhum próximo evento cadastrado."}
                        """;
                resposta = """
                        {"id":"chat-teste","object":"chat.completion","created":1795440000,
                         "model":"openai/gpt-oss-20b","choices":[{"index":0,"message":%s,
                         "finish_reason":"%s"}],
                         "usage":{"prompt_tokens":10,"completion_tokens":5,"total_tokens":15}}
                        """.formatted(mensagem, consulta ? "tool_calls" : "stop");
            }
            if (atrasoAtual > 0) {
                try {
                    Thread.sleep(atrasoAtual);
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    exchange.close();
                    return;
                }
            }
            byte[] corpo = resposta.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            try (exchange) {
                exchange.sendResponseHeaders(status, corpo.length);
                exchange.getResponseBody().write(corpo);
            } catch (IOException exception) {
                exchange.close();
            }
        });
        servidor.start();
        registry.add("spring.ai.openai.base-url",
                () -> "http://127.0.0.1:" + servidor.getAddress().getPort() + "/v1");
    }

    @BeforeEach
    void preparar() {
        requisicoes.clear();
        falhasRestantes.set(0);
        ferramentaSolicitada.set(false);
        codigoFalha = 0;
        atraso = 0;
        chamarFerramenta = false;
    }

    @AfterAll
    static void encerrarProvedor() {
        if (servidor != null) servidor.stop(0);
        executor.shutdownNow();
    }

    @Test
    void executaFerramentaSemReenviarReasoningContentAoProvedor() {
        chamarFerramenta = true;
        assertThat(chatService.responderPergunta("Quais eventos disponíveis?"))
                .isEqualTo("Nenhum próximo evento cadastrado.");

        assertThat(requisicoes).hasSize(2);
        String segundaRequisicao = requisicoes.get(1);
        assertThat(segundaRequisicao).doesNotContain("reasoning_content");
        assertThat(JsonPath.<List<String>>read(segundaRequisicao, "$.messages[*].role"))
                .containsExactly("system", "system", "user", "assistant", "tool");
        assertThat(JsonPath.<String>read(segundaRequisicao, "$.messages[3].tool_calls[0].function.name"))
                .isEqualTo("listarProximosEventos");
        assertThat(JsonPath.<String>read(segundaRequisicao, "$.messages[4].content")).isEqualTo("[]");
        assertThat(JsonPath.<String>read(segundaRequisicao, "$.reasoning_effort")).isEqualTo("low");
        assertThat(JsonPath.<Number>read(segundaRequisicao, "$.temperature").doubleValue()).isEqualTo(0.2);
    }

    @ParameterizedTest
    @ValueSource(ints = {429, 503})
    void recuperaFalhaTemporariaComUmaRetentativa(int status) {
        codigoFalha = status;
        falhasRestantes.set(1);

        assertThat(chatService.responderPergunta("Quais eventos temos?"))
                .isEqualTo("Nenhum próximo evento cadastrado.");
        assertThat(requisicoes).hasSize(2);
    }

    @Test
    void limitaRetentativasQuandoOProvedorContinuaIndisponivel() {
        codigoFalha = 503;
        falhasRestantes.set(10);

        assertThatThrownBy(() -> chatService.responderPergunta("Quais eventos temos?"))
                .isInstanceOf(AssistenteIndisponivelException.class).hasCauseInstanceOf(RuntimeException.class);
        assertThat(requisicoes).hasSize(2);
    }

    @Test
    void preservaOIntervaloDeEsperaQuandoOLimitePersiste() {
        codigoFalha = 429;
        falhasRestantes.set(10);

        assertThatThrownBy(() -> chatService.responderPergunta("Quais eventos temos?"))
                .isInstanceOfSatisfying(AssistenteLimiteExcedidoException.class,
                        exception -> assertThat(exception.getTentarNovamenteEmSegundos()).isEqualTo(1));
        assertThat(requisicoes).hasSize(2);
    }

    @Test
    void naoRepeteRequisicaoRejeitadaPorErroPermanente() {
        codigoFalha = 400;
        falhasRestantes.set(10);

        assertThatThrownBy(() -> chatService.responderPergunta("Quais eventos temos?"))
                .isInstanceOf(AssistenteIndisponivelException.class);
        assertThat(requisicoes).hasSize(1);
    }

    @Test
    void interrompeEsperaQuandoOProvedorNaoRespondeNoPrazo() {
        atraso = 3000;
        long inicio = System.nanoTime();

        assertThatThrownBy(() -> chatService.responderPergunta("Quais eventos temos?"))
                .isInstanceOf(AssistenteIndisponivelException.class);
        assertThat(java.time.Duration.ofNanos(System.nanoTime() - inicio).toSeconds()).isLessThan(8);
        assertThat(requisicoes.size()).isBetween(1, 2);
    }
}
