package com.minoh.lumiris_backend.dto.out;

import java.time.Instant;
import java.util.UUID;

public record DppFormResponse(
        UUID id,
        String productName,
        Instant createdAt
) {}