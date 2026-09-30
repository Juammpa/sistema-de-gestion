package org.sistemagestion.backend.dto.butcher_shop;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ButcherShopRequestDto {

    @NotBlank(message = "El nombre es obligatorio.")
    private String name;

    @NotBlank(message = "La dirección es obligatoria.")
    private String address;

    @NotNull(message = "El ID del cliente es obligatorio.")
    @Positive(message = "El ID del cliente debe ser positivo.")
    private Long clientId;
}
