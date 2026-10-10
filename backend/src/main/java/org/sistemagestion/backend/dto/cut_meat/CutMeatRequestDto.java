package org.sistemagestion.backend.dto.cut_meat;

import java.math.BigDecimal;

import jakarta.validation.constraints.*;
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

    @NotBlank(message = "El nombre es obligatorio.")
    @Size(min = 2, max = 60, message = "El nombre debe tener entre 2 y 60 caracteres")
    private String cutMeatName;

    @NotNull(message = "El precio es obligatorio.")
    @Positive(message = "El precio debe ser mayor a 0.")
    @DecimalMax(value = "9999999.99", message = "El precio no puede superar a $9.999.999,99.")
    @Digits(integer = 7, fraction = 2, message = "El precio admite hasta 2 decimales.")
    private BigDecimal pricePerKilo;

    @NotNull(message = "La especie es obligatoria.")
    private Specie specie;

}
