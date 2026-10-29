package org.sistemagestion.backend.mapper;

import org.sistemagestion.backend.dto.custom_payment_method.CustomPaymentMethodRequestDto;
import org.sistemagestion.backend.dto.custom_payment_method.CustomPaymentMethodResponseDto;
import org.sistemagestion.backend.model.CustomPaymentMethod;
import org.springframework.stereotype.Component;

@Component
public class CustomPaymentMethodMapper {

    public CustomPaymentMethod toEntity(CustomPaymentMethodRequestDto request) {

        return CustomPaymentMethod.builder()
                .name(request.getName().trim())
                .isActive(true)
                .build();
    }

    public CustomPaymentMethodResponseDto toDto(CustomPaymentMethod method) {

        return CustomPaymentMethodResponseDto.builder()
                .id(method.getId())
                .name(method.getName())
                .isActive(method.getIsActive())
                .build();
    }

}
