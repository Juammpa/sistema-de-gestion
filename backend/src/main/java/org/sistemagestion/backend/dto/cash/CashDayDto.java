package org.sistemagestion.backend.dto.cash;

import java.time.LocalDate;
import java.util.List;

public record CashDayDto(
        LocalDate date,
        List<CashPaymentDto> payments,
        List<CashWithdrawalDto> withdrawals,
        double total,           // Todos los pagos de clientes del dia
        double cash,            // Solo los recibidos en efectivo
        double withdrawn,       // Efectivo pagado a proveedores
        double closingBalance   // Caja acumulada al cierre del dia
) {
}
