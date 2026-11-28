package com.wavepass.model;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Locale;

public enum EstadoBrasil {
    AC("Acre"),
    AL("Alagoas"),
    AP("Amapá"),
    AM("Amazonas"),
    BA("Bahia"),
    CE("Ceará"),
    DF("Distrito Federal"),
    ES("Espírito Santo"),
    GO("Goiás"),
    MA("Maranhão"),
    MT("Mato Grosso"),
    MS("Mato Grosso do Sul"),
    MG("Minas Gerais"),
    PA("Pará"),
    PB("Paraíba"),
    PR("Paraná"),
    PE("Pernambuco"),
    PI("Piauí"),
    RJ("Rio de Janeiro"),
    RN("Rio Grande do Norte"),
    RS("Rio Grande do Sul"),
    RO("Rondônia"),
    RR("Roraima"),
    SC("Santa Catarina"),
    SP("São Paulo"),
    SE("Sergipe"),
    TO("Tocantins");

    private final String nome;

    EstadoBrasil(String nome) {
        this.nome = nome;
    }

    public String nomeNormalizado() {
        return normalizar(nome);
    }

    public static EstadoBrasil porNomeOuSigla(String valor) {
        String normalizado = normalizar(valor);
        return Arrays.stream(values())
                .filter(estado -> normalizar(estado.name()).equals(normalizado)
                        || estado.nomeNormalizado().equals(normalizado))
                .findFirst().orElse(null);
    }

    public static String normalizar(String valor) {
        if (valor == null) return "";
        return Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .trim().replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);
    }
}
