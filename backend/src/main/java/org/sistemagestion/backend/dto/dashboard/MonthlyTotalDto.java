package org.sistemagestion.backend.dto.dashboard;

import java.math.BigDecimal;

// Total de un mes (resultado de una consulta agrupada por año y mes)
public record MonthlyTotalDto(
    Integer year,
    Integer month,
    BigDecimal total
) { }
