package com.billMate.billing.infrastructure.rest.error;

import com.billMate.billing.infrastructure.rest.dto.ApiError;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static net.logstash.logback.argument.StructuredArguments.kv;

@RestControllerAdvice
public class GlobalExceptionHandler {

        private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        @ExceptionHandler(EntityNotFoundException.class)
        public ResponseEntity<ApiError> handleEntityNotFound(EntityNotFoundException ex) {
                log.warn("Resource not found", kv("exception", ex.getClass().getSimpleName()), kv("error", ex.getMessage()));
                ApiError error = new ApiError()
                                .status(HttpStatus.NOT_FOUND.name())
                                .code(HttpStatus.NOT_FOUND.value())
                                .message(ErrorMessages.RESOURCE_NOT_FOUND)
                                .errors(List.of(ex.getMessage()))
                                .timestamp(OffsetDateTime.now());

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
                log.warn("Validation error", kv("fieldErrors", ex.getBindingResult().getFieldErrors().size()));
                List<String> validationErrors = ex.getBindingResult().getFieldErrors().stream()
                                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                                .toList();

                ApiError error = new ApiError()
                                .status(HttpStatus.BAD_REQUEST.name())
                                .code(HttpStatus.BAD_REQUEST.value())
                                .message("La solicitud contiene errores de validación.")
                                .errors(validationErrors)
                                .timestamp(OffsetDateTime.now());

                return ResponseEntity.badRequest().body(error);
        }

        @ExceptionHandler(ConstraintViolationException.class)
        public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex) {
                List<String> errors = ex.getConstraintViolations().stream()
                                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                                .collect(Collectors.toList());

                ApiError apiError = new ApiError()
                                .status(HttpStatus.BAD_REQUEST.name())
                                .code(HttpStatus.BAD_REQUEST.value())
                                .message(ErrorMessages.CONSTRAINT_VIOLATION)
                                .errors(errors)
                                .timestamp(OffsetDateTime.now());

                return ResponseEntity.badRequest().body(apiError);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiError> handleUnexpected(Exception ex) {
                log.error("Unexpected error", kv("exception", ex.getClass().getSimpleName()), kv("error", ex.getMessage()), ex);
                ApiError apiError = new ApiError()
                                .status(HttpStatus.INTERNAL_SERVER_ERROR.name())
                                .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                .message(ErrorMessages.UNEXPECTED_ERROR)
                                .errors(List.of(ex.getMessage()))
                                .timestamp(OffsetDateTime.now());

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiError);
        }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ApiError> handleInvalidJson(HttpMessageNotReadableException ex) {
                log.warn("Invalid JSON received", kv("exception", ex.getClass().getSimpleName()), kv("error", ex.getMessage()));
                ApiError apiError = new ApiError()
                                .status(HttpStatus.BAD_REQUEST.name())
                                .code(HttpStatus.BAD_REQUEST.value())
                                .message(ErrorMessages.INVALID_JSON)
                                .errors(List.of("Formato JSON inválido o mal formado"))
                                .timestamp(OffsetDateTime.now());

                return ResponseEntity.badRequest().body(apiError);
        }

        @ExceptionHandler(IllegalStateException.class)
        public ResponseEntity<ApiError> handleIllegalState(IllegalStateException ex) {
                log.warn("Operation not allowed", kv("exception", ex.getClass().getSimpleName()), kv("error", ex.getMessage()));
                ApiError error = new ApiError()
                                .status(HttpStatus.BAD_REQUEST.name())
                                .code(HttpStatus.BAD_REQUEST.value())
                                .message("Operación no permitida")
                                .errors(List.of(ex.getMessage()))
                                .timestamp(OffsetDateTime.now());

                return ResponseEntity.badRequest().body(error);
        }

        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<ApiError> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
                log.warn("Data integrity violation", kv("exception", ex.getClass().getSimpleName()), kv("error", ex.getMostSpecificCause().getMessage()));
                ApiError apiError = new ApiError()
                                .status(HttpStatus.CONFLICT.name())
                                .code(HttpStatus.CONFLICT.value())
                                .message(ErrorMessages.DATA_INTEGRITY_VIOLATION)
                                .errors(List.of(ex.getMostSpecificCause().getMessage()))
                                .timestamp(OffsetDateTime.now());

                return ResponseEntity.status(HttpStatus.CONFLICT).body(apiError);
        }

}
