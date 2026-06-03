package br.com.gado.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("Requisicao invalida: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("mensagem", traduz(e.getMessage(), "Requisicao invalida.")));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException e) {
        log.error("Erro de runtime: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("mensagem", traduz(e.getMessage(), "Erro inesperado no servidor.")));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAll(Exception e) {
        log.error("Erro nao tratado: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("mensagem", traduz(e.getMessage(), "Erro interno no servidor.")));
    }

    private static String traduz(String mensagem, String fallback) {
        if (mensagem == null || mensagem.isBlank()) {
            return fallback;
        }

        if (mensagem.contains("No enum constant")) {
            return "Existe um registro com valor invalido para um campo de tipo. "
                    + "Contate o suporte para corrigir o cadastro.";
        }

        if (mensagem.toLowerCase().contains("could not execute statement")
                || mensagem.toLowerCase().contains("constraint")) {
            return "Nao foi possivel salvar os dados. Verifique se nao existe duplicidade ou referencia invalida.";
        }

        if (mensagem.toLowerCase().contains("dataintegrityviolation")) {
            return "Dados invalidos para esta operacao.";
        }

        return mensagem;
    }
}
