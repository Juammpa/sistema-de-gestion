package org.sistemagestion.backend.service.purchase_type;

import org.sistemagestion.backend.dto.purchase_type.PurchaseTypeRequestDto;
import org.sistemagestion.backend.dto.purchase_type.PurchaseTypeResponseDto;
import org.sistemagestion.backend.model.enums.Specie;

import java.util.List;

public interface IPurchaseTypeService {

    PurchaseTypeResponseDto create(PurchaseTypeRequestDto request);
    PurchaseTypeResponseDto getById(Long id);

    // specie null: devuelve las dos especies
    List<PurchaseTypeResponseDto> getAll(Specie specie);

    PurchaseTypeResponseDto update(Long id, PurchaseTypeRequestDto request);
    void activate(Long id);
    void delete(Long id);

    boolean existsByNameAndSpecie(String name, Specie specie, Long excludeId);
}
