package com.minoh.lumiris_backend.controller;

import com.minoh.lumiris_backend.dto.in.DppFormRequest;
import com.minoh.lumiris_backend.dto.out.DppFormResponse;
import com.minoh.lumiris_backend.service.DppFormService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dpp-forms")
@RequiredArgsConstructor
public class DppFormController {

    private final DppFormService dppFormService;

    @PostMapping
    ResponseEntity<DppFormResponse> create(@Valid @RequestBody DppFormRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dppFormService.create(request));
    }

    @GetMapping
    ResponseEntity<List<DppFormResponse>> findAll() {
        return ResponseEntity.ok(dppFormService.findAll());
    }
}