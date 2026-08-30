package com.shopkeeper.app.exception;

import com.shopkeeper.app.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ===== Registration / account =====
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Object>> handle(UserAlreadyExistsException ex) {
        return respond(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Object>> handle(UsernameAlreadyExistsException ex) {
        return respond(HttpStatus.CONFLICT, ex.getMessage());
    }

    // ===== OTP =====
    @ExceptionHandler(InvalidOTPException.class)
    public ResponseEntity<ApiResponse<Object>> handle(InvalidOTPException ex) {
        log.warn("Invalid OTP attempt: {}", ex.getMessage());
        return respond(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(OTPExpiredException.class)
    public ResponseEntity<ApiResponse<Object>> handle(OTPExpiredException ex) {
        return respond(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(OTPAttemptExceededException.class)
    public ResponseEntity<ApiResponse<Object>> handle(OTPAttemptExceededException ex) {
        log.warn("OTP attempt limit exceeded: {}", ex.getMessage());
        return respond(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage());
    }

    // ===== Tokens =====
    @ExceptionHandler(InvalidRegistrationTokenException.class)
    public ResponseEntity<ApiResponse<Object>> handle(InvalidRegistrationTokenException ex) {
        return respond(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(InvalidResetTokenException.class)
    public ResponseEntity<ApiResponse<Object>> handle(InvalidResetTokenException ex) {
        return respond(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // ===== Auth =====
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handle(InvalidCredentialsException ex) {
        log.warn("Authentication failure: {}", ex.getMessage());
        return respond(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthException(AuthenticationException ex) {
        log.warn("Authentication failure: invalid username or password");
        return respond(HttpStatus.UNAUTHORIZED, "Invalid username or password");
    }

    // ===== Generic =====
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handle(ResourceNotFoundException ex) {
        return respond(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Object>> handle(ApiException ex) {
        return respond(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidation(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .toList();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.failure("Validation failed", errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneric(Exception ex) {
        log.error("Unhandled exception", ex);
        return respond(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong. Please try again.");
    }

    private ResponseEntity<ApiResponse<Object>> respond(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(ApiResponse.failure(message));
    }
}
