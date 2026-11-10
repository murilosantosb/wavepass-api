package com.wavepass.exception;

import com.wavepass.dto.ErroRespostaDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroRespostaDTO> tratarErroInesperado(
            Exception ex,
            HttpServletRequest request) {

        ErroRespostaDTO resposta = new ErroRespostaDTO(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Ocorreu um erro interno no servidor. Tente novamente mais tarde.",
                request.getRequestURI(),
                null
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resposta);
    }
}