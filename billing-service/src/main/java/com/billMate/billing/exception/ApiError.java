package com.billMate.billing.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiError {

    private HttpStatus status;
    private String message;
    private OffsetDateTime timestamp;
    private List<String> errors;

    public static ApiError of(HttpStatus status, String message, List<String> errors) {
        return ApiError.builder()
                .status(status)
                .message(message)
                .timestamp(OffsetDateTime.now())
                .errors(errors)
                .build();
    }
}
