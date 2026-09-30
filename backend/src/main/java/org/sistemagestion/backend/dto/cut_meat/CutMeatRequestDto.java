package org.sistemagestion.backend.dto.cut_meat;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sistemagestion.backend.model.enums.Specie;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CutMeatRequestDto {

    @NotNull(message = "El nombre es obligatorio.")
    private String cutMeatName;

    @NotNull(message = "El precio es obligatorio.")
    @PositiveOrZero(message = "El precio debe ser positivo.")
    private Double pricePerKilo;

    @NotNull(message = "La especie es obligatoria.")
    private Specie specie;

}
