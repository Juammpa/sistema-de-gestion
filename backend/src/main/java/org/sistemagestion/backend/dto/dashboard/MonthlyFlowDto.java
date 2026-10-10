package org.sistemagestion.backend.dto.dashboard;

import java.math.BigDecimal;

// Los cuatro totales de un mes, listos para graficar
public record MonthlyFlowDto(
        int year,
        int month,
        BigDecimal sales,
        BigDecimal purchases,
        BigDecimal collections, // pagos recibidos de clientes
        BigDecimal payments     // pagos hechos a proveedores
) {
}
