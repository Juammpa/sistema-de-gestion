package org.sistemagestion.backend.dto.butcher_shop;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ButcherShopUpdateRequestDto {

    @NotBlank(message = "El nombre es obligatorio.")
    @Size(min = 2, max = 60, message = "El nombre debe tener entre 2 y 60 caracteres.")
    private String name;

    @NotBlank(message = "La dirección es obligatoria.")
    @Size(min = 3, max = 120, message = "El nombre debe tener entre 3 y 120 caracteres.")
    private String address;

}
