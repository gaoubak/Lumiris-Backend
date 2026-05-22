package com.minoh.lumiris_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.minoh.lumiris_backend.dto.in.DppFormRequest;
import com.minoh.lumiris_backend.dto.out.DppFormResponse;
import com.minoh.lumiris_backend.service.DppFormService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DppFormControllerTest {

    @Mock
    private DppFormService dppFormService;

    @InjectMocks
    private DppFormController dppFormController;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(dppFormController)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void create_shouldReturn201_whenValidRequest() throws Exception {
        DppFormRequest request = new DppFormRequest("Fairphone 5");
        DppFormResponse response = new DppFormResponse(UUID.randomUUID(), "Fairphone 5", Instant.now());

        when(dppFormService.create(any())).thenReturn(response);

        ResultActions result = mockMvc.perform(post("/api/dpp-forms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.productName").value("Fairphone 5"))
                .andExpect(jsonPath("$.id").isNotEmpty());

        verify(dppFormService).create(any());
    }

    @Test
    void create_shouldReturn400_whenProductNameIsBlank() throws Exception {
        DppFormRequest request = new DppFormRequest("");

        ResultActions result = mockMvc.perform(post("/api/dpp-forms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400_whenBodyIsEmpty() throws Exception {
        ResultActions result = mockMvc.perform(post("/api/dpp-forms")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"));

        result.andExpect(status().isBadRequest());
    }

    @Test
    void findAll_shouldReturn200_withList() throws Exception {
        DppFormResponse response = new DppFormResponse(UUID.randomUUID(), "Fairphone 5", Instant.now());

        when(dppFormService.findAll()).thenReturn(List.of(response));

        ResultActions result = mockMvc.perform(get("/api/dpp-forms"));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productName").value("Fairphone 5"));

        verify(dppFormService).findAll();
    }
}
