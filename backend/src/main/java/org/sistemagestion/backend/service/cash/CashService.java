package org.sistemagestion.backend.service.cash;

import lombok.RequiredArgsConstructor;
import org.sistemagestion.backend.common.Decimals;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        BigDecimal closingBalance = openingBalance(start);
        List<CashDayDto> days = new ArrayList<>();

        for (LocalDate date: dates) {
            List<CashPaymentDto> payments = paymentsByDate.getOrDefault(date, List.of());
            List<CashWithdrawalDto> withdrawals = withdrawalsByDate.getOrDefault(date, List.of());

            // Obtenemos el total
            BigDecimal total = sum(payments.stream().map(CashPaymentDto::amount));
            // Obtenemos el total de pagos recibidos (no tenemos en cuenta 'Transferencia a proveedor')
            BigDecimal cash = sum(payments.stream()
                    .filter(payment -> payment.paymentType() != PaymentType.SUPPLIER_TRANSFER)
                    .map(CashPaymentDto::amount));
            BigDecimal withdrawn = sum(withdrawals.stream().map(CashWithdrawalDto::amount));

            closingBalance = closingBalance.add(cash).subtract(withdrawn);

            days.add(new CashDayDto(date, payments, withdrawals, total, cash, withdrawn, closingBalance));
        }

        Collections.reverse(days);
        return days;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getCurrentBalance() {
        BigDecimal cashIn = Decimals.moneyOrZero(cashRepository.sumClientPayments(PaymentType.SUPPLIER_TRANSFER));
        BigDecimal cashOut = Decimals.moneyOrZero(cashRepository.sumSupplierPayments(PaymentType.CASH));

        return cashIn.subtract(cashOut);
    }

    // Efectivo acumulado hasta el dia anterior al rango (la caja arranca en cero)
    private BigDecimal openingBalance(LocalDate start) {
        BigDecimal cashIn = Decimals.moneyOrZero(cashRepository.sumClientPaymentsBefore(PaymentType.SUPPLIER_TRANSFER, start));
        BigDecimal cashOut = Decimals.moneyOrZero(cashRepository.sumSupplierPaymentsBefore(PaymentType.CASH, start));

        return cashIn.subtract(cashOut);
    }

    private BigDecimal sum(Stream<BigDecimal> amounts) {
        return amounts.filter(Objects::nonNull).reduce(Decimals.MONEY_ZERO, BigDecimal::add);
    }

    // Valida que un pago en efectivo a un proveedor no supere la caja disponible
    // 'existing' es el pago antes de editarlo (null si es un alta), para no descontarlo dos veces
    public void assertSufficientBalance(CurrentAccount account, PaymentType newType, BigDecimal newAmount, Payment existing) {

        if (newType != PaymentType.CASH || !supplierRepository.existsByCurrentAccountId(account.getId())) {
            return;
        }

        boolean wasCashWithdrawal = existing != null && existing.getPaymentType() == PaymentType.CASH;
        BigDecimal previousAmount = wasCashWithdrawal ? Decimals.moneyOrZero(existing.getAmount()) : Decimals.MONEY_ZERO;
        BigDecimal available = getCurrentBalance().add(previousAmount);

        if (newAmount.compareTo(available) > 0) {
            throw new BusinessRuleException(String.format(
                    Locale.forLanguageTag("es-AR"),
                    "Saldo de caja insuficiente: disponible $%.2f, solicitado $%.2f", available, newAmount));
        }

    }

}
