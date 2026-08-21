package org.sistemagestion.backend.dto.supplier;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SupplierResponseDto {

    private Long id;
    private String companyName;
    private String phoneNumber;
    private Long currentAccountId;
}
