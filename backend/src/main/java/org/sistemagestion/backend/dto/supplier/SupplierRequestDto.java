package org.sistemagestion.backend.dto.supplier;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SupplierRequestDto {

    @NotBlank(message = "The company name is mandatory")
    private String companyName;

    @NotBlank(message = "The phone number is mandatory")
    @Pattern(regexp = "^\\+?[0-9. ()-]{7,25}$", message = "Invalid phone number format")
    private String phoneNumber;
}
