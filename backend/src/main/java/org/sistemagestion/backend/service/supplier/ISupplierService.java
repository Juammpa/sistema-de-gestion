package org.sistemagestion.backend.service.supplier;

import org.sistemagestion.backend.dto.supplier.SupplierRequestDto;
import org.sistemagestion.backend.dto.supplier.SupplierResponseDto;

import java.util.List;

public interface ISupplierService {

    SupplierResponseDto createSupplier(SupplierRequestDto request);
    SupplierResponseDto getById(Long id);
    List<SupplierResponseDto> getAllSuppliers();
    SupplierResponseDto updateSupplier(Long id, SupplierRequestDto request);
    void deleteById(Long id);

    boolean existsByCompanyName(String companyName, Long excludeId);
}
