package com.deroyque.activation.api;
import com.deroyque.activation.service.*;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail validation(MethodArgumentNotValidException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Um ou mais campos não passaram na validação.");
        problem.setTitle("Dados inválidos");
        problem.setProperty("errors", ex.getBindingResult().getFieldErrors().stream().map(e -> Map.of("field", e.getField(), "message", e.getDefaultMessage())).toList()); return problem;
    }
    @ExceptionHandler(RequestNotFoundException.class) ProblemDetail notFound(RuntimeException ex) { return detail(HttpStatus.NOT_FOUND, "Solicitação não encontrada", ex.getMessage()); }
    @ExceptionHandler(ConflictingExternalIdException.class) ProblemDetail conflict(RuntimeException ex) { return detail(HttpStatus.CONFLICT, "Solicitação duplicada", ex.getMessage()); }
    @ExceptionHandler(Exception.class)
    ProblemDetail unexpected(Exception ex) {
        var traceId = UUID.randomUUID().toString();
        log.error("Unexpected error. traceId={}", traceId, ex);
        var problem = detail(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno", "Ocorreu um erro inesperado. Informe o traceId ao suporte.");
        problem.setProperty("traceId", traceId);
        return problem;
    }
    private ProblemDetail detail(HttpStatus status, String title, String message) { var p = ProblemDetail.forStatusAndDetail(status, message); p.setTitle(title); return p; }
}
