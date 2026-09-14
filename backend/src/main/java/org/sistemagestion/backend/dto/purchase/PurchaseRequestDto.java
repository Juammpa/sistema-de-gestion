package org.sistemagestion.backend.dto.purchase;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sistemagestion.backend.model.enums.PurchaseType;
import org.sistemagestion.backend.model.enums.Specie;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PurchaseRequestDto {

    @NotNull(message = "The purchaseType is mandatory")
    private PurchaseType purchaseType;

    @NotNull(message = "The unit is mandatory")
    @Positive(message = "The unit must be positive")
    private Integer unit;

    @NotNull(message = "The kilograms are mandatory")
    @Positive(message = "The kilograms must be positive")
    private Double kilograms;

    @NotNull(message = "The price is mandatory")
    @Positive(message = "The price must be positive")
    private Double pricePerKilo;

    @NotNull(message = "The currentAccountId is mandatory")
    private Long currentAccountId;

}
