package org.sistemagestion.backend.service.cash;

import lombok.RequiredArgsConstructor;
import org.sistemagestion.backend.dto.cash.CashDayDto;
import org.sistemagestion.backend.dto.cash.CashPaymentDto;
import org.sistemagestion.backend.dto.cash.CashWithdrawalDto;
import org.sistemagestion.backend.exception.BusinessRuleException;
import org.sistemagestion.backend.model.enums.PaymentType;
import org.sistemagestion.backend.repository.CashRepository;
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

    @Override
    @Transactional
    public List<CashDayDto> getCashDays(LocalDate from, LocalDate to) {

        LocalDate end = to != null ? to: LocalDate.now();
        LocalDate start = from != null ? from: end.minusDays(DEFAULT_DAYS -1);

        if (start.isAfter(end)) {
            throw new BusinessRuleException("The 'from' date cannot be after the 'to' date");
        }

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

            double total = payments.stream().mapToDouble(CashPaymentDto::amount).sum();
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

    // Efectivo acumulado hasta el dia anterior al rango (la caja arranca en cero)
    private double openingBalance(LocalDate start) {
        double cashIn = orZero(cashRepository.sumClientPaymentsBefore(PaymentType.SUPPLIER_TRANSFER, start));
        double cashOut = orZero(cashRepository.sumSupplierPaymentsBefore(PaymentType.CASH, start));

        return cashIn - cashOut;
    }

    private double orZero(Double value) {
        return value != null ? value : 0.0;
    }


}
