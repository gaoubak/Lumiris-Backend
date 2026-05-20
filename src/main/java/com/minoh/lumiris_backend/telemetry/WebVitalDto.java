package com.minoh.lumiris_backend.telemetry;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record WebVitalDto(
        @NotBlank @Pattern(regexp = "CLS|LCP|FID|INP|TTFB|FCP") String name,
        @NotNull @DecimalMin("0.0") @DecimalMax("1000000.0") Double value,
        @NotBlank @Pattern(regexp = "good|needs-improvement|poor") String rating,
        @NotBlank String sessionId,
        @NotBlank @Pattern(regexp = "admin|site|client|mobile") String app,
        @NotBlank String route,
        String navigationType,
        @NotNull Long timestamp
) {}
