package org.sistemagestion.backend.dto.butcher_shop;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ButcherShopUpdateRequestDto {

    @NotBlank(message = "El nombre es obligatorio.")
    private String name;

    @NotBlank(message = "La dirección es obligatoria.")
    private String address;

}
