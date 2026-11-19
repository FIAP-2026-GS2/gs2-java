package br.com.sampatech.portal.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class FallbackExceptionHandler {

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NoResourceFoundException ex, HttpServletRequest req)
            throws NoResourceFoundException {
        if (!isApi(req)) {
            throw ex;
        }
        return build(HttpStatus.NOT_FOUND, "Rota não encontrada", req);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex,
                                                                HttpServletRequest req)
            throws HttpRequestMethodNotSupportedException {
        if (!isApi(req)) {
            throw ex;
        }
        return build(HttpStatus.METHOD_NOT_ALLOWED, "Método " + ex.getMethod() + " não é suportado nesta rota", req);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex,
                                                                    HttpServletRequest req)
            throws HttpMediaTypeNotSupportedException {
        if (!isApi(req)) {
            throw ex;
        }
        return build(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Envie o corpo da requisição como application/json", req);
    }

    private boolean isApi(HttpServletRequest req) {
        return req.getRequestURI().startsWith("/api/");
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, HttpServletRequest req) {
        return ResponseEntity.status(status).body(ErrorResponse.withFields(
                status.value(), status.getReasonPhrase(), message, req.getRequestURI(), List.of()));
    }
}
