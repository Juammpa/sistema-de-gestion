package org.sistemagestion.backend.dto.cut_meat;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sistemagestion.backend.model.enums.CutMeatType;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CutMeatRequestDto {

    @NotNull(message = "The cutMeatType is mandatory")
    @Enumerated(EnumType.STRING)
    private CutMeatType cutMeatType;

    @NotNull(message = "The price is mandatory")
    @PositiveOrZero(message = "The price must be positive")
    private Double pricePerKilo;

}
