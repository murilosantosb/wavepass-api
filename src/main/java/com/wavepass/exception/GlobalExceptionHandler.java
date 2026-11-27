package com.wavepass.exception;

import com.wavepass.dto.ErroRespostaDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AssistenteIndisponivelException.class)
    public ResponseEntity<ErroRespostaDTO> tratarAssistenteIndisponivel(
            AssistenteIndisponivelException ex, HttpServletRequest request) {
        log.error("Falha ao gerar resposta do assistente", ex);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new ErroRespostaDTO(
                LocalDateTime.now(), HttpStatus.SERVICE_UNAVAILABLE.value(),
                ex.getMessage(), request.getRequestURI(), null));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErroRespostaDTO> tratarAutenticacao(
            AuthenticationException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErroRespostaDTO(
                LocalDateTime.now(), HttpStatus.UNAUTHORIZED.value(),
                "E-mail ou senha inválidos", request.getRequestURI(), null));
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroRespostaDTO> tratarRecursoNaoEncontrado(
            RecursoNaoEncontradoException ex,
            HttpServletRequest request) {

        ErroRespostaDTO resposta = new ErroRespostaDTO(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resposta);
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroRespostaDTO> tratarRegraNegocio(
            RegraNegocioException ex,
            HttpServletRequest request) {

        ErroRespostaDTO resposta = new ErroRespostaDTO(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resposta);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroRespostaDTO> tratarErrosDeValidacao(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        List<ErroRespostaDTO.CampoErroDTO> detalhesErros = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> new ErroRespostaDTO.CampoErroDTO(fe.getField(), fe.getDefaultMessage()))
                .toList();

        ErroRespostaDTO resposta = new ErroRespostaDTO(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Erro de validação nos campos informados",
                request.getRequestURI(),
                detalhesErros
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resposta);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErroRespostaDTO> tratarErroInesperado(
            AccessDeniedException ex,
            HttpServletRequest request) {


        log.error("Erro inesperado em {} {}", request.getMethod(), request.getRequestURI(), ex);


        ErroRespostaDTO resposta = new ErroRespostaDTO(
                LocalDateTime.now(),
                HttpStatus.FORBIDDEN.value(),
                "Ocorreu um erro interno no servidor. Tente novamente mais tarde.",
                request.getRequestURI(),
                null
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resposta);
    }
}
