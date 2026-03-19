package com.bombazine.setlist_builder.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    //Format of every error response
    public record ErrorResponse(String message, int status, LocalDateTime timestamp) {}

    @ExceptionHandler(Exceptions.SongNotFoundException.class)
    public ResponseEntity<ErrorResponse> handle(Exceptions.SongNotFoundException exception){
        return error(exception.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exceptions.SetlistNotFoundException.class)
    public ResponseEntity<ErrorResponse> handle(Exceptions.SetlistNotFoundException exception) {
        return error(exception.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exceptions.SetlistAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handle(Exceptions.SetlistAlreadyExistsException exception) {
        return error(exception.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(Exceptions.InsufficientSongsException.class)
    public ResponseEntity<ErrorResponse> handle(Exceptions.InsufficientSongsException exception) {
        return error(exception.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(Exceptions.SpotifyApiException.class)
    public ResponseEntity<ErrorResponse> handle(Exceptions.SpotifyApiException exception) {
        return error(exception.getMessage(), HttpStatus.BAD_GATEWAY);
    }

    //Method to return a map of field names to error messages since there can be multiple validation failures at once
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException exception) {
        Map<String, String> errors = exception
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(FieldError::getField,
                        fieldError -> Objects.requireNonNullElse(fieldError.getDefaultMessage(), "Validation failed"), (existing, replacement) -> existing));
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(Exceptions.InvalidCredentialsException.class)
    public ResponseEntity<String> handleInvalidCredentials(Exceptions.InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return error(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    //Helper method to avoid code repetition of ResponseEntity construction
    private ResponseEntity<ErrorResponse> error(String message, HttpStatus status) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(message, status.value(), LocalDateTime.now()));
    }

}
