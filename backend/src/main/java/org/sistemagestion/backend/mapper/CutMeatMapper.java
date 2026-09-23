package org.sistemagestion.backend.mapper;

import org.sistemagestion.backend.dto.cut_meat.CutMeatRequestDto;
import org.sistemagestion.backend.dto.cut_meat.CutMeatResponseDto;
import org.sistemagestion.backend.model.CutMeat;
import org.springframework.stereotype.Component;

@Component
public class CutMeatMapper {

    public CutMeat toEntity(CutMeatRequestDto requestDto) {
        return CutMeat.builder()
                .cutMeatName(requestDto.getCutMeatName())
                .pricePerKilo(requestDto.getPricePerKilo())
                .specie(requestDto.getSpecie())
                .isActive(true)
                .build();
    }

    public CutMeatResponseDto toDto(CutMeat cutMeat) {
        return CutMeatResponseDto.builder()
                .id(cutMeat.getId())
                .cutMeatName(cutMeat.getCutMeatName())
                .pricePerKilo(cutMeat.getPricePerKilo())
                .specie(cutMeat.getSpecie())
                .isActive(cutMeat.getIsActive())
                .build();
    }
}
