package org.sistemagestion.backend.service.dashboard;

import lombok.RequiredArgsConstructor;
import org.sistemagestion.backend.dto.dashboard.*;
import org.sistemagestion.backend.model.*;
import org.sistemagestion.backend.repository.ButcherShopRepository;
import org.sistemagestion.backend.repository.DashboardRepository;
import org.sistemagestion.backend.repository.MovementRepository;
import org.sistemagestion.backend.repository.SupplierRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class DashboardService implements IDashboardService{

        private static final int MONTHS = 12;
        private static final int TOP_SIZE = 5;
        private static final int RECENT_SIZE = 5;

        private final DashboardRepository dashboardRepository;
        private final MovementRepository movementRepository;
        private final ButcherShopRepository butcherShopRepository;
        private final SupplierRepository supplierRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardDto getDashboard() {

        // Ultimos 12 meses, incluyendo el actual
        YearMonth current = YearMonth.now();
        YearMonth first = current.minusMonths(MONTHS - 1);
        LocalDate from = first.atDay(1);
        LocalDate to = current.atEndOfMonth();

        Map<YearMonth, Double> sales = byMonth(dashboardRepository.salesByMonth(from, to));
        Map<YearMonth, Double> purchases = byMonth(dashboardRepository.purchasesByMonth(from, to));
        Map<YearMonth, Double> collections = byMonth(dashboardRepository.collectionsByMonth(from, to));
        Map<YearMonth, Double> payments = byMonth(dashboardRepository.supplierPaymentsByMonth(from, to));

        // Los meses sin movimientos quedan en cero para que el grafico no tenga huecos
        List<MonthlyFlowDto> months = IntStream.range(0, MONTHS)
                .mapToObj(i -> {
                    YearMonth month = first.plusMonths(i);
                    return new MonthlyFlowDto(
                            month.getYear(), month.getMonthValue(),
                            sales.getOrDefault(month, 0.0),
                            purchases.getOrDefault(month, 0.0),
                            collections.getOrDefault(month, 0.0),
                            payments.getOrDefault(month, 0.0));
                }).toList();

        double receivable = orZero(dashboardRepository.totalClientBalance());
        double payable = orZero(dashboardRepository.totalSupplierBalance());

        // Saldo a fin del mes anterior = saldo actual menos lo que se movio en el mes en curso
        MonthlyFlowDto currentMonth = months.get(MONTHS - 1);
        double previousReceivable = receivable - (currentMonth.sales() - currentMonth.collections());
        double previousPayable = payable - (currentMonth.purchases() - currentMonth.payments());

        List<RecentMovementDto> recent = movementRepository.findRecent(PageRequest.of(0, RECENT_SIZE))
                .stream()
                .map(this::toRecentMovement)
                .toList();

        return new DashboardDto(
                receivable, payable, previousReceivable, previousPayable, months,
                dashboardRepository.topClientDebts(PageRequest.of(0, TOP_SIZE)),
                dashboardRepository.topSupplierDebts(PageRequest.of(0, TOP_SIZE)),
                recent
        );
    }

    private Map<YearMonth, Double> byMonth(List<MonthlyTotalDto> totals) {
        return totals.stream().collect(Collectors.toMap(
                total -> YearMonth.of(total.year(), total.month()),
                MonthlyTotalDto::total
        ));
    }

    // Arma la fila de "movimientos recientes": de quien es la cuenta y que tipo de movimiento es
    private RecentMovementDto toRecentMovement(Movement movement) {

        String detail = switch (movement) {
            case Sale sale -> sale.getCutMeatName();
            case Purchase purchase -> purchase.getPurchaseType() != null ? purchase.getPurchaseType().name() : null;
            case Payment payment -> payment.getPaymentType().name();
            default -> null;
        };

        Long accountId = movement.getCurrentAccount().getId();
        Optional<ButcherShop> shop = butcherShopRepository.findByCurrentAccountId(accountId);

        if (shop.isPresent()) {
            Client client = shop.get().getClient();
            return new RecentMovementDto(
                    movement.getId(), movement.getDate(),
                    movement instanceof Sale ? DashboardMovementKind.SALE : DashboardMovementKind.COLLECTION,
                    client.getLastName() + " " + client.getName(),
                    detail, movement.getAmount());
        }

        String supplierName = supplierRepository.findByCurrentAccountId(accountId)
                .map(Supplier::getCompanyName)
                .orElse("-");

        return new RecentMovementDto(
                movement.getId(), movement.getDate(),
                movement instanceof Purchase ? DashboardMovementKind.PURCHASE : DashboardMovementKind.PAYMENT,
                supplierName, detail, movement.getAmount());
    }

    private double orZero(Double value) {
        return value != null ? value : 0.0;
    }
}
