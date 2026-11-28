package com.wavepass.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ChatRespostaFormatterTest {

    private final ChatRespostaFormatter formatter = new ChatRespostaFormatter();

    @ParameterizedTest
    @MethodSource("precos")
    void formataMoedaSemAlterarOValor(String entrada, String esperado) {
        assertThat(formatter.formatar(entrada)).isEqualTo(esperado);
    }

    static Stream<Arguments> precos() {
        return Stream.of(
                Arguments.of("R$ 65.00", "R$ 65,00"),
                Arguments.of("R$ 65,00", "R$ 65,00"),
                Arguments.of("R$ 65.0", "R$ 65,00"),
                Arguments.of("R$\u202f55.00", "R$ 55,00"),
                Arguments.of("R$ 120", "R$ 120,00"),
                Arguments.of("R$ 1234.56", "R$ 1.234,56"),
                Arguments.of("R$ 1,234.56", "R$ 1.234,56"),
                Arguments.of("R$ 1.234,56", "R$ 1.234,56"),
                Arguments.of("R$ 1.234", "R$ 1.234,00"),
                Arguments.of("R$ 0.00", "R$ 0,00"));
    }

    @Test
    void corrigeOsProblemasCapturadosNosTestesReais() {
        String entrada = """
                Foram encontrados 1 evento que correspondem à sua pesquisa.

                **Universo Imersivo**

                - **Quando:** 06/03/2027 às 13:00
                - **Preço:** a partir de R$ 45.00
                - **Classificação:** 0 anos
                """;
        String esperado = """
                Foi encontrado 1 evento que corresponde à sua pesquisa.

                **Universo Imersivo**

                - **Quando:** 06/03/2027 às 13:00
                - **Preço:** a partir de R$ 45,00
                - **Classificação:** Livre
                """;
        assertThat(formatter.formatar(entrada)).isEqualTo(esperado);
        assertThat(formatter.formatar(esperado)).isEqualTo(esperado);
    }

    @Test
    void preservaPluralIdadesDatasENomesComZero() {
        String resposta = """
                Foram encontrados 10 eventos que atendem à sua pesquisa.
                **Evento 0 anos de história**
                - **Quando:** 04/04/2027 às 22:00
                - **Classificação:** 10 anos
                Classificação: 12 anos
                Preço: Não informado
                """;
        assertThat(formatter.formatar(resposta)).isEqualTo(resposta);
    }

    @Test
    void trataAsVariantesDeSingularEClassificacao() {
        assertThat(formatter.formatar("Foram encontrados 1 evento em Campinas que atende aos critérios."))
                .isEqualTo("Foi encontrado 1 evento em Campinas que atende aos critérios.");
        assertThat(formatter.formatar("Foram encontrados 1 evento que atendem aos critérios."))
                .isEqualTo("Foi encontrado 1 evento que atende aos critérios.");
        assertThat(formatter.formatar("Faixa etária: 0 anos\nClassificação indicativa: 0"))
                .isEqualTo("Faixa etária: Livre\nClassificação indicativa: Livre");
    }
}
