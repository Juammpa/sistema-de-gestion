package org.sistemagestion.backend.dto.supplier;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sistemagestion.backend.model.enums.SupplierType;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SupplierRequestDto {

    @NotBlank(message = "El nombre es obligatorio.")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres.")
    private String companyName;

    @NotBlank(message = "El número de télefono es obligatorio.")
    @Pattern(regexp = "^\\+?[0-9. ()-]{7,25}$", message = "El formato del télefono es inválido.")
    private String phoneNumber;

    @NotNull(message = "El tipo de proveedor es obligatorio.")
    private SupplierType supplierType;
}
