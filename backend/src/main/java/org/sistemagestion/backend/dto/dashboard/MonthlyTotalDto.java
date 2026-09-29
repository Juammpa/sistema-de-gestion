package org.sistemagestion.backend.dto.dashboard;

// Total de un mes (resultado de una consulta agrupada por año y mes)
public record MonthlyTotalDto(
    Integer year,
    Integer month,
    Double total
) { }
