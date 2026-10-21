package org.sistemagestion.backend.mapper;

import org.sistemagestion.backend.dto.purchase_type.PurchaseTypeRequestDto;
import org.sistemagestion.backend.dto.purchase_type.PurchaseTypeResponseDto;
import org.sistemagestion.backend.model.PurchaseType;
import org.springframework.stereotype.Component;

@Component
public class PurchaseTypeMapper {

    public PurchaseType toEntity(PurchaseTypeRequestDto request) {

        return PurchaseType.builder()
                .name(request.getName())
                .specie(request.getSpecie())
                .isActive(true)
                .build();
    }

    public PurchaseTypeResponseDto toDto(PurchaseType purchaseType) {

        return PurchaseTypeResponseDto.builder()
                .id(purchaseType.getId())
                .name(purchaseType.getName())
                .specie(purchaseType.getSpecie())
                .isActive(purchaseType.getIsActive())
                .build();
    }

}
