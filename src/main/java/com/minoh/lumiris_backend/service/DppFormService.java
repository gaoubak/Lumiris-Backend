package com.minoh.lumiris_backend.service;

import com.minoh.lumiris_backend.dto.in.DppFormRequest;
import com.minoh.lumiris_backend.dto.out.DppFormResponse;
import com.minoh.lumiris_backend.mapper.DppFormMapper;
import com.minoh.lumiris_backend.repository.DppFormRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DppFormService {

    private final DppFormRepository dppFormRepository;
    private final DppFormMapper dppFormMapper;

    public DppFormResponse create(DppFormRequest request) {
        return dppFormMapper.toResponse(
                dppFormRepository.save(dppFormMapper.toEntity(request))
        );
    }

    public List<DppFormResponse> findAll() {
        return dppFormRepository.findAll()
                .stream()
                .map(dppFormMapper::toResponse)
                .toList();
    }
}
