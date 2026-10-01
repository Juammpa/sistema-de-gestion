package org.sistemagestion.backend.mapper;

import org.sistemagestion.backend.dto.supplier.SupplierRequestDto;
import org.sistemagestion.backend.dto.supplier.SupplierResponseDto;
import org.sistemagestion.backend.model.Supplier;
import org.springframework.stereotype.Component;

@Component
public class SupplierMapper {

    public Supplier toSupplier(SupplierRequestDto requestDto) {
        return Supplier.builder()
                .companyName(requestDto.getCompanyName())
                .phoneNumber(requestDto.getPhoneNumber())
                .supplierType(requestDto.getSupplierType())
                .build();
    }

    public SupplierResponseDto toDto(Supplier supplier) {
        return SupplierResponseDto.builder()
                .id(supplier.getId())
                .companyName(supplier.getCompanyName())
                .phoneNumber(supplier.getPhoneNumber())
                .supplierType(supplier.getSupplierType())
                .currentAccountId(supplier.getCurrentAccount().getId())
                .currentBalance(supplier.getCurrentAccount().getCurrentBalance())
                .build();
    }

}
