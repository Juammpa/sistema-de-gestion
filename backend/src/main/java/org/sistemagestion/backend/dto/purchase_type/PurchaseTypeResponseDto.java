package org.sistemagestion.backend.dto.purchase_type;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sistemagestion.backend.model.enums.Specie;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PurchaseTypeResponseDto {

    private Long id;
    private String name;
    private Specie specie;
    private Boolean isActive;
}
