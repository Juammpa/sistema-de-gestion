package org.sistemagestion.backend.dto.cash;

import java.math.BigDecimal;

import java.time.LocalDate;
import java.util.List;

// Resumen de un dia de caja: sus movimientos y los totales ya calculados
public record CashDayDto(
        LocalDate date,
        List<CashPaymentDto> payments,
        List<CashWithdrawalDto> withdrawals,
        BigDecimal total,           // Todos los pagos de clientes del dia
        BigDecimal cash,            // Todoo lo cobrado a clientes ese dia, menos transf. a proveedor
        BigDecimal withdrawn,       // Efectivo pagado a proveedores
        BigDecimal closingBalance   // Caja acumulada al cierre del dia
) {
}
