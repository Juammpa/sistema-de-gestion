package org.sistemagestion.backend.dto.dashboard;

import java.util.List;

public record DashboardDto(
        double receivable,          // por cobrar (saldo de clientes)
        double payable,             // por pagar (saldo de proveedores)
        double previousReceivable,  // por cobrar a fin del mes anterior
        double previousPayable,     // por pagar a fin del mes anterior
        List<MonthlyFlowDto> months, // ultimos 12 meses, del mas viejo al actual
        List<DebtRankingDto> topClients,
        List<DebtRankingDto> topSuppliers,
        List<RecentMovementDto> recentMovements
) { }
