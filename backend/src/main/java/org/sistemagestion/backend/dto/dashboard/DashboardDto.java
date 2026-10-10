package org.sistemagestion.backend.dto.dashboard;

import java.math.BigDecimal;

import java.util.List;

public record DashboardDto(
        BigDecimal receivable,          // por cobrar (saldo de clientes)
        BigDecimal payable,             // por pagar (saldo de proveedores)
        BigDecimal previousReceivable,  // por cobrar a fin del mes anterior
        BigDecimal previousPayable,     // por pagar a fin del mes anterior
        List<MonthlyFlowDto> months, // ultimos 12 meses, del mas viejo al actual
        List<DebtRankingDto> topClients,
        List<DebtRankingDto> topSuppliers,
        List<RecentMovementDto> recentMovements
) { }
