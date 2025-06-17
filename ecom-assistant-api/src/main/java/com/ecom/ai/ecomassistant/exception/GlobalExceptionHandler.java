package com.ecom.ai.ecomassistant.exception;

import com.ecom.ai.ecomassistant.common.dto.ErrorResponse;
import com.ecom.ai.ecomassistant.core.exception.EntityExistException;
import com.ecom.ai.ecomassistant.core.exception.EntityNotFoundException;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UnauthenticatedException.class)
    public ResponseEntity<ErrorResponse<String>> handleUnauthenticatedException(UnauthenticatedException e) {
        ErrorResponse<String> response = new ErrorResponse<>(
                401,
                "認證失敗",
                List.of(e.getMessage())
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse<String>> handleUnauthorizedException(UnauthorizedException e) {
        ErrorResponse<String> response = new ErrorResponse<>(
                403,
                "沒有權限執行此操作",
                List.of(e.getMessage())
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse<ValidationError>> handleValidationException(MethodArgumentNotValidException ex) {
        List<ValidationError> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ValidationError(error.getField(), error.getDefaultMessage()))
                .collect(Collectors.toList());

        ErrorResponse<ValidationError> response =
                new ErrorResponse<>(400, "validation failed", validationErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse<?>> handelEntityNotFoundException(EntityNotFoundException ex) {
        ErrorResponse<String> response =
                new ErrorResponse<>(404, ex.getMessage(), List.of(ex.getMessage()));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(EntityExistException.class)
    public ResponseEntity<ErrorResponse<?>> handelEntityExistException(EntityExistException ex) {
        ErrorResponse<String> response =
                new ErrorResponse<>(400, ex.getMessage(), List.of(ex.getMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

}
