package org.sistemagestion.backend.dto.sales;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sistemagestion.backend.dto.stock.AvailableCutDto;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class SaleResponseDto {

    private Long id;
    private Long clientId;
    private LocalDate date;
    private Long cutMeatId;
    private String cutMeatName;
    private Integer unit;
    private BigDecimal kilograms;
    private BigDecimal pricePerKilo;
    private BigDecimal subtotal;

    // Unidades de stock de la venta.
    // Solo viene al pedir una venta o al crearla/editarla; en los listados es null
    private List<AvailableCutDto> stockCuts;

}
