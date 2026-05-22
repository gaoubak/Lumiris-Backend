package com.minoh.lumiris_backend.dto.out;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
        int status,
        String message,
        Instant timestamp,
        Map<String, String> errors
) {
    public ErrorResponse(int status, String message) {
        this(status, message, Instant.now(), null);
    }

    public ErrorResponse(int status, String message, Map<String, String> errors) {
        this(status, message, Instant.now(), errors);
    }
}