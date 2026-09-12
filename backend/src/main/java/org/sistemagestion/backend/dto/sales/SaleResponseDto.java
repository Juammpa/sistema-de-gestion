package org.sistemagestion.backend.dto.sales;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sistemagestion.backend.model.enums.CutMeatType;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class SaleResponseDto {

    private Long id;
    private Long clientId;
    private LocalDate date;
    private CutMeatType cutMeatType;
    private Integer unit;
    private Double kilograms;
    private Double pricePerKilo;
    private Double subtotal;

}
