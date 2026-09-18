package org.sistemagestion.backend.dto.cut_meat;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CutMeatRequestDto {

    @NotNull(message = "The cutMeatName is mandatory")
    private String cutMeatName;

    @NotNull(message = "The price is mandatory")
    @PositiveOrZero(message = "The price must be positive")
    private Double pricePerKilo;

}
