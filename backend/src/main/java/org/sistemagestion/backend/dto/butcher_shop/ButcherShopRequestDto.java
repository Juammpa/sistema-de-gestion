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

    @NotBlank(message = "The name is mandatory")
    private String name;

    @NotBlank(message = "The address is mandatory")
    private String address;

    @NotNull(message = "The client ID is mandatory")
    @Positive(message = "The client ID must be positive")
    private Long clientId;
}
