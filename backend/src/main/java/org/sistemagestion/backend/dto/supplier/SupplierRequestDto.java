package org.sistemagestion.backend.dto.supplier;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sistemagestion.backend.model.enums.SupplierType;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SupplierRequestDto {

    @NotBlank(message = "El nombre es obligatorio.")
    private String companyName;

    @NotBlank(message = "El número de télefono es obligatorio.")
    @Pattern(regexp = "^\\+?[0-9. ()-]{7,25}$", message = "El formato es inválido.")
    private String phoneNumber;

    @NotNull(message = "El tipo de proveedor es obligatorio.")
    private SupplierType supplierType;
}
