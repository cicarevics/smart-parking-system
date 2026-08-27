package com.smartparking.parkingservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(LotNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleLotNotFound(LotNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("detail", ex.getMessage()));
    }

    @ExceptionHandler(SpotNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleSpotNotFound(SpotNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("detail", ex.getMessage()));
    }

    @ExceptionHandler(SpotNumberAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleSpotNumberAlreadyExists(SpotNumberAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("detail", ex.getMessage()));
    }

    @ExceptionHandler(SpotAlreadyClaimedException.class)
    public ResponseEntity<Map<String, String>> handleSpotAlreadyClaimed(SpotAlreadyClaimedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("detail", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> toSnakeCase(error.getField()) + ": " + error.getDefaultMessage())
                .orElse("Validation failed");
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of("detail", message));
    }

    private static String toSnakeCase(String camelCase) {
        return camelCase.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase();
    }
}
