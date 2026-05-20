package com.minoh.lumiris_backend.telemetry;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TelemetryControllerTest {

    private MockMvc mvc;
    private MeterRegistry registry;
    private ObjectMapper json;

    @BeforeEach
    void setup() {
        registry = new SimpleMeterRegistry();
        json = new ObjectMapper();
        mvc = MockMvcBuilders.standaloneSetup(new TelemetryController(registry))
                .setMessageConverters(new org.springframework.http.converter.json.MappingJackson2HttpMessageConverter(json))
                .build();
    }

    private Map<String, Object> validBody() {
        return Map.of(
                "name", "CLS",
                "value", 0.1,
                "rating", "good",
                "sessionId", "abc",
                "app", "site",
                "route", "/",
                "timestamp", 1700000000000L
        );
    }

    @Test
    void happyPath_returns204_andRecordsMetrics() throws Exception {
        mvc.perform(post("/api/telemetry/web-vitals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(validBody())))
                .andExpect(status().isNoContent());

        assertThat(registry.find("web_vitals_total").counter()).isNotNull();
        assertThat(registry.find("web_vitals_value").summary()).isNotNull();
        assertThat(registry.find("web_vitals_total").counter().count()).isEqualTo(1.0);
    }

    @Test
    void invalidName_returns400() throws Exception {
        var body = new java.util.HashMap<>(validBody());
        body.put("name", "UNKNOWN");
        mvc.perform(post("/api/telemetry/web-vitals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void valueTooLarge_returns400() throws Exception {
        var body = new java.util.HashMap<>(validBody());
        body.put("value", 9_999_999.0);
        mvc.perform(post("/api/telemetry/web-vitals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void invalidApp_returns400() throws Exception {
        var body = new java.util.HashMap<>(validBody());
        body.put("app", "unknown-app");
        mvc.perform(post("/api/telemetry/web-vitals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }
}
