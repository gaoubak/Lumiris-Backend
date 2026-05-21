package com.minoh.lumiris_backend.service;

import com.minoh.lumiris_backend.dto.in.DppFormRequest;
import com.minoh.lumiris_backend.dto.out.DppFormResponse;
import com.minoh.lumiris_backend.entity.DppForm;
import com.minoh.lumiris_backend.mapper.DppFormMapper;
import com.minoh.lumiris_backend.repository.DppFormRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DppFormServiceTest {

    @Mock
    private DppFormRepository dppFormRepository;

    @Mock
    private DppFormMapper dppFormMapper;

    @InjectMocks
    private DppFormService dppFormService;

    @Test
    void create_shouldSaveAndReturnResponse() {
        // given
        DppFormRequest request = new DppFormRequest("Fairphone 5");
        DppForm entity = new DppForm();
        DppForm savedEntity = new DppForm();
        savedEntity.setId(UUID.randomUUID());
        savedEntity.setProductName("Fairphone 5");
        DppFormResponse expected = new DppFormResponse(savedEntity.getId(), "Fairphone 5", null);

        when(dppFormMapper.toEntity(request)).thenReturn(entity);
        when(dppFormRepository.save(entity)).thenReturn(savedEntity);
        when(dppFormMapper.toResponse(savedEntity)).thenReturn(expected);

        // when
        DppFormResponse result = dppFormService.create(request);

        // then
        assertThat(result.id()).isEqualTo(expected.id());
        assertThat(result.productName()).isEqualTo("Fairphone 5");
        verify(dppFormMapper).toEntity(request);
        verify(dppFormRepository).save(entity);
        verify(dppFormMapper).toResponse(savedEntity);
    }

    @Test
    void findAll_shouldReturnMappedResponses() {
        // given
        DppForm entity = new DppForm();
        entity.setId(UUID.randomUUID());
        entity.setProductName("Fairphone 5");
        DppFormResponse expected = new DppFormResponse(entity.getId(), "Fairphone 5", null);

        when(dppFormRepository.findAll()).thenReturn(List.of(entity));
        when(dppFormMapper.toResponse(entity)).thenReturn(expected);

        // when
        List<DppFormResponse> result = dppFormService.findAll();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().productName()).isEqualTo("Fairphone 5");
        verify(dppFormRepository).findAll();
        verify(dppFormMapper).toResponse(entity);
    }
}