package org.sistemagestion.backend.service.cash;

import lombok.RequiredArgsConstructor;
import org.sistemagestion.backend.dto.cash.CashDayDto;
import org.sistemagestion.backend.dto.cash.CashPaymentDto;
import org.sistemagestion.backend.dto.cash.CashWithdrawalDto;
import org.sistemagestion.backend.exception.BusinessRuleException;
import org.sistemagestion.backend.model.CurrentAccount;
import org.sistemagestion.backend.model.Payment;
import org.sistemagestion.backend.model.enums.PaymentType;
import org.sistemagestion.backend.repository.CashRepository;
import org.sistemagestion.backend.repository.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CashService implements ICashService{

    private static final int DEFAULT_DAYS = 7;

    private final CashRepository cashRepository;
    private final SupplierRepository supplierRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CashDayDto> getCashDays(LocalDate from, LocalDate to) {

        // Filtros de fechas (desde / hasta)
        LocalDate end = to != null ? to: LocalDate.now();
        LocalDate start = from != null ? from: end.minusDays(DEFAULT_DAYS -1);

        // 'Desde' no puede ser mayor que 'Hasta'
        if (start.isAfter(end)) {
            throw new BusinessRuleException("La fecha «desde» no puede ser posterior a la fecha «hasta»");
        }

        // (Fecha, Lista de pagos de clientes)
        Map<LocalDate, List<CashPaymentDto>> paymentsByDate =
                cashRepository.findClientPayments(start, end).stream()
                        .collect(Collectors.groupingBy(CashPaymentDto::date));

        Map<LocalDate, List<CashWithdrawalDto>> withdrawalsByDate =
                cashRepository.findSupplierPayments(PaymentType.CASH, start, end).stream()
                        .collect(Collectors.groupingBy(CashWithdrawalDto::date));

        // Solo los dias con movimientos, en orden cronologico para acumular la caja
        SortedSet<LocalDate> dates = new TreeSet<>(paymentsByDate.keySet());
        dates.addAll(withdrawalsByDate.keySet());

        double closingBalance = openingBalance(start);
        List<CashDayDto> days = new ArrayList<>();

        for (LocalDate date: dates) {
            List<CashPaymentDto> payments = paymentsByDate.getOrDefault(date, List.of());
            List<CashWithdrawalDto> withdrawals = withdrawalsByDate.getOrDefault(date, List.of());

            // Obtenemos el total
            double total = payments.stream().mapToDouble(CashPaymentDto::amount).sum();
            // Obtenemos el total de pagos recibidos (no tenemos en cuenta 'Transferencia a proveedor')
            double cash = payments.stream()
                    .filter(payment -> payment.paymentType() != PaymentType.SUPPLIER_TRANSFER)
                    .mapToDouble(CashPaymentDto::amount).sum();
            double withdrawn = withdrawals.stream().mapToDouble(CashWithdrawalDto::amount).sum();

            closingBalance += cash - withdrawn;

            days.add(new CashDayDto(date, payments, withdrawals, total, cash, withdrawn, closingBalance));
        }

        Collections.reverse(days);
        return days;
    }

    @Override
    @Transactional(readOnly = true)
    public double getCurrentBalance() {
        double cashIn = orZero(cashRepository.sumClientPayments(PaymentType.SUPPLIER_TRANSFER));
        double cashOut = orZero(cashRepository.sumSupplierPayments(PaymentType.CASH));

        return cashIn - cashOut;
    }

    // Efectivo acumulado hasta el dia anterior al rango (la caja arranca en cero)
    private double openingBalance(LocalDate start) {
        double cashIn = orZero(cashRepository.sumClientPaymentsBefore(PaymentType.SUPPLIER_TRANSFER, start));
        double cashOut = orZero(cashRepository.sumSupplierPaymentsBefore(PaymentType.CASH, start));

        return cashIn - cashOut;
    }

    private double orZero(Double value) {
        return value != null ? value : 0.0;
    }

    // Valida que un pago en efectivo a un proveedor no supere la caja disponible
    // 'existing' es el pago antes de editarlo (null si es un alta), para no descontarlo dos veces
    public void assertSufficientBalance(CurrentAccount account, PaymentType newType, Double newAmount, Payment existing) {

        if (newType != PaymentType.CASH || !supplierRepository.existsByCurrentAccountId(account.getId())) {
            return;
        }

        boolean wasCashWithdrawal = existing != null && existing.getPaymentType() == PaymentType.CASH;
        double previousAmount = wasCashWithdrawal ? existing.getAmount() :  0.0;
        double available = getCurrentBalance() + previousAmount;

        if (newAmount > available) {
            throw new BusinessRuleException(String.format(
                    Locale.forLanguageTag("es-AR"),
                    "Saldo de caja insuficiente: disponible $%.2f, solicitado $%.2f", available, newAmount));
        }

    }

}
