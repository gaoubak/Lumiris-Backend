package com.minoh.lumiris_backend.mapper;

import com.minoh.lumiris_backend.dto.in.DppFormRequest;
import com.minoh.lumiris_backend.dto.out.DppFormResponse;
import com.minoh.lumiris_backend.entity.DppForm;
import org.springframework.stereotype.Component;

@Component
public class DppFormMapper {

    public DppForm toEntity(DppFormRequest request) {
        DppForm dppForm = new DppForm();
        dppForm.setProductName(request.productName());
        return dppForm;
    }

    public DppFormResponse toResponse(DppForm dppForm) {
        return new DppFormResponse(
                dppForm.getId(),
                dppForm.getProductName(),
                dppForm.getCreatedAt()
        );
    }
}