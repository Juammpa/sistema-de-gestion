package org.sistemagestion.backend.dto.custom_payment_method;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CustomPaymentMethodResponseDto {

    private Long id;
    private String name;
    private Boolean isActive;
}
