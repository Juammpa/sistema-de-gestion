package org.sistemagestion.backend.dto.cut_meat;

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
    private Double pricePerKilo;
    private Boolean isActive;
    private Specie specie;

}
