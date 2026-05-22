package com.minoh.lumiris_backend.mapper;

import com.minoh.lumiris_backend.dto.in.DppFormRequest;
import com.minoh.lumiris_backend.dto.out.DppFormResponse;
import com.minoh.lumiris_backend.entity.DppForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DppFormMapperTest {

    private DppFormMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new DppFormMapper();
    }

    @Test
    void toEntity_shouldMapProductName() {
        // given
        DppFormRequest request = new DppFormRequest("Fairphone 5");

        // when
        DppForm result = mapper.toEntity(request);

        // then
        assertThat(result.getProductName()).isEqualTo("Fairphone 5");
        assertThat(result.getId()).isNull();
    }

    @Test
    void toResponse_shouldMapAllFields() {
        // given
        DppForm entity = new DppForm();
        UUID id = UUID.randomUUID();
        entity.setId(id);
        entity.setProductName("Fairphone 5");

        // when
        DppFormResponse result = mapper.toResponse(entity);

        // then
        assertThat(result.id()).isEqualTo(id);
        assertThat(result.productName()).isEqualTo("Fairphone 5");
        assertThat(result.createdAt()).isNull();
    }
}