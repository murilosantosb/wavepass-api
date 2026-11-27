package com.wavepass.exception;

public class AssistenteIndisponivelException extends RuntimeException {
    public AssistenteIndisponivelException() {
        super("O assistente está indisponível no momento. Tente novamente mais tarde.");
    }

    public AssistenteIndisponivelException(Throwable causa) {
        super("O assistente está indisponível no momento. Tente novamente mais tarde.", causa);
    }
}
