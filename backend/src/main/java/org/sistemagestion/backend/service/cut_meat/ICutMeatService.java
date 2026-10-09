package org.sistemagestion.backend.service.cut_meat;

import org.sistemagestion.backend.dto.cut_meat.CutMeatRequestDto;
import org.sistemagestion.backend.dto.cut_meat.CutMeatResponseDto;
import org.sistemagestion.backend.model.enums.Specie;

import java.util.List;

public interface ICutMeatService {

    CutMeatResponseDto createCutMeat(CutMeatRequestDto request);
    CutMeatResponseDto getCutMeatById(Long id);
    List<CutMeatResponseDto> getAllCutMeats();
    CutMeatResponseDto updateCutMeat(Long id, CutMeatRequestDto request);
    void activeCutMeatById(Long id);
    void deleteCutMeatById(Long id);

    boolean existsByNameAndSpecie(String name, Specie specie, Long excludeId);
}
