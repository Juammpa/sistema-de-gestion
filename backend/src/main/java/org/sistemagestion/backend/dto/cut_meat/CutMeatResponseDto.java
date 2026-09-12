package org.sistemagestion.backend.dto.cut_meat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sistemagestion.backend.model.enums.CutMeatType;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CutMeatResponseDto {

    private Long id;
    private CutMeatType cutMeatType;
    private Double pricePerKilo;

}
