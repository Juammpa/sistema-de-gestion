package org.sistemagestion.backend.dto.cut_meat;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sistemagestion.backend.model.enums.Specie;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CutMeatResponseDto {

    private Long id;
    private String cutMeatName;
    private BigDecimal pricePerKilo;
    private Boolean isActive;
    private Specie specie;

}
