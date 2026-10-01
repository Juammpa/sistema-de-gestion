package org.sistemagestion.backend.dto.supplier;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sistemagestion.backend.model.enums.SupplierType;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class SupplierResponseDto {

    private Long id;
    private String companyName;
    private String phoneNumber;
    private SupplierType supplierType;
    private Long currentAccountId;
    private Double currentBalance;
}
