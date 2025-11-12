package com.example.demo.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationException(MethodArgumentNotValidException ex,
                                                              HttpServletRequest request) {

        String fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        ApiError apiError = new ApiError(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                fieldErrors,
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleNotReadable(HttpMessageNotReadableException ex,
                                                      HttpServletRequest request) {

        ApiError apiError = new ApiError(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                "Request body is invalid or malformed",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(Exception ex,
                                                           HttpServletRequest request) {

        ApiError apiError = new ApiError(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                ex.getMessage(),  // derslerde göstermek için bırakabilirsin, prod'da gizlenir genelde
                request.getRequestURI()
        );

        ex.printStackTrace();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiError);
    }
}
