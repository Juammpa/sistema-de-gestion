package org.sistemagestion.backend.dto.dashboard;

// Los cuatro totales de un mes, listos para graficar
public record MonthlyFlowDto(
        int year,
        int month,
        double sales,
        double purchases,
        double collections, // pagos recibidos de clientes
        double payments     // pagos hechos a proveedores
) {
}
