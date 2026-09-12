package org.sistemagestion.backend.mapper;

import org.sistemagestion.backend.dto.cut_meat.CutMeatRequestDto;
import org.sistemagestion.backend.dto.cut_meat.CutMeatResponseDto;
import org.sistemagestion.backend.model.CutMeat;
import org.springframework.stereotype.Component;

@Component
public class CutMeatMapper {

    public CutMeat toEntity(CutMeatRequestDto requestDto) {
        return CutMeat.builder()
                .cutMeatType(requestDto.getCutMeatType())
                .pricePerKilo(requestDto.getPricePerKilo())
                .build();
    }

    public CutMeatResponseDto toDto(CutMeat cutMeat) {
        return CutMeatResponseDto.builder()
                .id(cutMeat.getId())
                .cutMeatType(cutMeat.getCutMeatType())
                .pricePerKilo(cutMeat.getPricePerKilo())
                .build();
    }
}
