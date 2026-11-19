package br.com.sampatech.portal.controller.web;

import br.com.sampatech.portal.exception.ErrorResponse;
import br.com.sampatech.portal.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@ControllerAdvice(basePackages = "br.com.sampatech.portal.controller.web")
public class WebExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String notFound() {
        return "error/404";
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> invalidBody(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<ErrorResponse.FieldErrorDetail> fields = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> new ErrorResponse.FieldErrorDetail(f.getField(), f.getDefaultMessage()))
                .toList();
        return ResponseEntity.badRequest().body(ErrorResponse.withFields(
                HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Dados inválidos", req.getRequestURI(), fields));
    }
}
