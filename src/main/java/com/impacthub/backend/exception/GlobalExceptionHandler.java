package com.impacthub.backend.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.impacthub.backend.dto.response.NgoLoginBlockedResponse;
import com.impacthub.backend.exception.NgoLoginBlockedException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private Map<String, String> errorBody(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        error.put("message", message);
        return error;
    }

    // Handle validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = errorBody("Validation failed");
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleUnreadableMessage(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getMostSpecificCause();

        if (cause instanceof InvalidFormatException invalidFormatException && !invalidFormatException.getPath().isEmpty()) {
            String fieldName = invalidFormatException.getPath().get(invalidFormatException.getPath().size() - 1).getFieldName();
            Map<String, String> error = errorBody("Invalid value for '" + fieldName + "'");
            return ResponseEntity.badRequest().body(error);
        }

        Map<String, String> error = errorBody(cause != null && cause.getMessage() != null
                ? cause.getMessage()
                : "Malformed JSON request");
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatusException(ResponseStatusException ex) {
        Map<String, String> error = errorBody(ex.getReason() != null ? ex.getReason() : "Request failed");
        return ResponseEntity.status(ex.getStatusCode()).body(error);
    }

    @ExceptionHandler(NgoLoginBlockedException.class)
    public ResponseEntity<NgoLoginBlockedResponse> handleNgoLoginBlockedException(NgoLoginBlockedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                new NgoLoginBlockedResponse(
                        ex.getCode(),
                        ex.getMessage(),
                        ex.getNgoStatus(),
                        ex.getRejectionReason(),
                        ex.getSuspensionReason()
                )
        );
    }

    // Handle generic runtime exceptions
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        Map<String, String> error = errorBody(ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
