package com.minoh.lumiris_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minoh.lumiris_backend.config.security.JwtAuthFilter;
import com.minoh.lumiris_backend.dto.in.DppFormRequest;
import com.minoh.lumiris_backend.dto.out.DppFormResponse;
import com.minoh.lumiris_backend.service.DppFormService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    value = DppFormController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthFilter.class)
)
class DppFormControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DppFormService dppFormService;

    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
            http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/auth/**").permitAll()
                    .anyRequest().authenticated()
                );
            return http.build();
        }
    }

    @Test
    @WithMockUser
    void create_shouldReturn201_whenValidRequest() throws Exception {
        // given
        DppFormRequest request = new DppFormRequest("Fairphone 5");
        DppFormResponse response = new DppFormResponse(UUID.randomUUID(), "Fairphone 5", Instant.now());

        when(dppFormService.create(any())).thenReturn(response);

        // when
        ResultActions result = mockMvc.perform(post("/api/dpp-forms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.productName").value("Fairphone 5"))
                .andExpect(jsonPath("$.id").isNotEmpty());

        verify(dppFormService).create(any());
    }

    @Test
    @WithMockUser
    void create_shouldReturn400_whenProductNameIsBlank() throws Exception {
        // given
        DppFormRequest request = new DppFormRequest("");

        // when
        ResultActions result = mockMvc.perform(post("/api/dpp-forms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.productName").isNotEmpty());
    }

    @Test
    @WithMockUser
    void create_shouldReturn400_whenBodyIsEmpty() throws Exception {
        // when
        ResultActions result = mockMvc.perform(post("/api/dpp-forms")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"));

        // then
        result.andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn403_whenNotAuthenticated() throws Exception {
        // given
        DppFormRequest request = new DppFormRequest("Fairphone 5");

        // when
        ResultActions result = mockMvc.perform(post("/api/dpp-forms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void findAll_shouldReturn200_withList() throws Exception {
        // given
        DppFormResponse response = new DppFormResponse(UUID.randomUUID(), "Fairphone 5", Instant.now());

        when(dppFormService.findAll()).thenReturn(List.of(response));

        // when
        ResultActions result = mockMvc.perform(get("/api/dpp-forms"));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productName").value("Fairphone 5"));

        verify(dppFormService).findAll();
    }
}