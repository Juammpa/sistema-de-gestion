package org.sistemagestion.backend.dto.sales;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SaleRequestDto {

    @NotNull(message = "The clientId is mandatory")
    private Long clientId;

    @NotNull(message = "The butcherShopId is mandatory")
    private Long butcherShopId;

    @NotNull(message = "The cutMeatId is mandatory")
    private Long cutMeatId;

    @NotNull(message = "The kilograms are mandatory")
    @Positive(message = "The kilograms must be positive")
    private Double kilograms;

    @NotNull(message = "The unit is mandatory")
    @Positive(message = "The unit must be positive")
    private Integer unit;

}
