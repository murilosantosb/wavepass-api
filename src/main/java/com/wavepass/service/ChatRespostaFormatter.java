package com.wavepass.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.regex.Pattern;

@Component
public class ChatRespostaFormatter {

    private static final Pattern PRECO = Pattern.compile("R\\$\\h*(\\d+(?:[.,]\\d+)*)");
    private static final Pattern INTRODUCAO_SINGULAR = Pattern.compile(
            "(?iu)\\bForam\\h+encontrados\\h+1\\h+eventos?\\b");
    private static final Pattern CONCORDANCIA_SINGULAR = Pattern.compile(
            "(?iu)(\\b1\\h+evento\\h+que\\h+)(atendem|correspondem)\\b");
    private static final Pattern CLASSIFICACAO_LIVRE = Pattern.compile(
            "(?iu)(\\b(?:Classificação(?:\\h+indicativa)?|Faixa\\h+etária)(?:\\*\\*)?\\h*:\\h*(?:\\*\\*\\h*)*)0(?:\\h+anos)?\\b");

    public String formatar(String resposta) {
        String formatada = PRECO.matcher(resposta).replaceAll(resultado -> {
            String valor = resultado.group(1);
            int separador = Math.max(valor.lastIndexOf('.'), valor.lastIndexOf(','));
            int casasDecimais = valor.length() - separador - 1;
            if (separador >= 0 && casasDecimais >= 1 && casasDecimais <= 2) {
                valor = valor.substring(0, separador).replace(".", "").replace(",", "")
                        + "." + valor.substring(separador + 1);
            } else {
                valor = valor.replace(".", "").replace(",", "");
            }
            String preco = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"))
                    .format(new BigDecimal(valor)).replace('\u00a0', ' ');
            return java.util.regex.Matcher.quoteReplacement(preco);
        });
        formatada = INTRODUCAO_SINGULAR.matcher(formatada).replaceAll("Foi encontrado 1 evento");
        formatada = CONCORDANCIA_SINGULAR.matcher(formatada).replaceAll(resultado ->
                resultado.group(1) + (resultado.group(2).equalsIgnoreCase("atendem") ? "atende" : "corresponde"));
        return CLASSIFICACAO_LIVRE.matcher(formatada).replaceAll("$1Livre");
    }
}
