package org.sistemagestion.backend.dto.client;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ClientCreateRequestDto {

    @NotBlank(message = "El nombre es obligatorio.")
    @Size(min = 2, max = 50, message = "El nombre debe contener entre 2 y 50 caracteres")
    @Pattern(regexp = "^\\p{L}[\\p{L} '.-]*$", message = "El nombre solo puede contener letras, espacios, apóstrofes, puntos y guiones.")
    private String name;

    @NotBlank(message = "El apellido es obligatorio.")
    @Size(min = 2, max = 50, message = "El apellido debe contener entre 2 y 50 caracteres")
    @Pattern(regexp = "^\\p{L}[\\p{L} '.-]*$", message = "El apellido solo puede contener letras, espacios, apóstrofes, puntos y guiones.")
    private String lastName;

    @NotBlank(message = "El número de télefono es obligatorio.")
    @Pattern(regexp = "^\\+?[0-9. ()-]{7,25}$", message = "El formato del télefono es inválido.")
    private String phoneNumber;

    @NotBlank(message = "El nombre de la sucursal es obligatorio.")
    @Size(min = 2, max = 60, message = "El nombre debe tener entre 2 y 60 caracteres.")
    private String butcherShopName;

    @NotBlank(message = "La dirección de la sucursal es obligatorio.")
    @Size(min = 3, max = 120, message = "El nombre debe tener entre 3 y 120 caracteres.")
    private String butcherShopAddress;
}
