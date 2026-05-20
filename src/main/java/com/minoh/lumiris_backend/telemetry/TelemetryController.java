package com.minoh.lumiris_backend.telemetry;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/telemetry")
public class TelemetryController {

    private static final Logger log = LoggerFactory.getLogger(TelemetryController.class);

    private final MeterRegistry registry;

    public TelemetryController(MeterRegistry registry) {
        this.registry = registry;
    }

    @PostMapping("/web-vitals")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void webVitals(@Valid @RequestBody WebVitalDto dto) {
        String navType = dto.navigationType() == null ? "unknown" : dto.navigationType();

        Counter.builder("web_vitals_total")
                .tag("name", dto.name())
                .tag("rating", dto.rating())
                .tag("app", dto.app())
                .tag("navigation_type", navType)
                .register(registry)
                .increment();

        DistributionSummary.builder("web_vitals_value")
                .tag("name", dto.name())
                .tag("app", dto.app())
                .register(registry)
                .record(dto.value());

        log.info("web_vital received name={} value={} rating={} app={} route={} session={} navType={} ts={}",
                dto.name(), dto.value(), dto.rating(), dto.app(), dto.route(), dto.sessionId(), navType, dto.timestamp());
    }
}
