package org.sistemagestion.backend.dto.purchase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PurchaseResponseDto {

    private Long id;
    private Long currentAccountId;
    private LocalDate date;
    private Integer unit;
    private Double kilograms;
    private Double pricePerKilo;
    private Double subtotal;
}
