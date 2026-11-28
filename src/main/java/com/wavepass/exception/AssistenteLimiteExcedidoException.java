package com.wavepass.exception;

public class AssistenteLimiteExcedidoException extends RuntimeException {

    private final long tentarNovamenteEmSegundos;

    public AssistenteLimiteExcedidoException(Throwable causa, long tentarNovamenteEmSegundos) {
        super("O assistente recebeu muitas solicitações. Aguarde alguns instantes e tente novamente.", causa);
        this.tentarNovamenteEmSegundos = tentarNovamenteEmSegundos;
    }

    public long getTentarNovamenteEmSegundos() {
        return tentarNovamenteEmSegundos;
    }
}
