package org.sistemagestion.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ClientRequestDto {

    @NotBlank(message = "The name is mandatory")
    private String name;

    @NotBlank(message = "The lastName is mandatory")
    private String lastName;

    @NotBlank(message = "The phoneNumber is mandatory")
    @Pattern(regexp = "^\\+?[0-9. ()-]{7,25}$", message = "Invalid phone number format")
    private String phoneNumber;
}
