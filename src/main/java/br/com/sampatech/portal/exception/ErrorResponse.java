package br.com.sampatech.portal.exception;


import java.time.Instant;
import java.util.List;

public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldErrorDetail> fields
) {
    public record FieldErrorDetail(String field, String message) {}

    public static ErrorResponse withFields(int status, String error, String message,
                                           String path, List<FieldErrorDetail> fields) {
        return new ErrorResponse(Instant.now(), status, error, message, path, fields);
    }
}
