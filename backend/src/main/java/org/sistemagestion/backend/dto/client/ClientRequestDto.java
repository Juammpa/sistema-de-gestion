package org.sistemagestion.backend.dto.client;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ClientRequestDto {

    @NotBlank(message = "El nombre es obligatorio.")
    private String name;

    @NotBlank(message = "El apellido es obligatorio.")
    private String lastName;

    @NotBlank(message = "El número de télefono es obligatorio.")
    @Pattern(regexp = "^\\+?[0-9. ()-]{7,25}$", message = "El formato es inválido.")
    private String phoneNumber;
}
