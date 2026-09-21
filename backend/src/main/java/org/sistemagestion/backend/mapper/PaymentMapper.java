package org.sistemagestion.backend.mapper;

import org.sistemagestion.backend.dto.payment.PaymentRequestDto;
import org.sistemagestion.backend.dto.payment.PaymentResponseDto;
import org.sistemagestion.backend.model.Client;
import org.sistemagestion.backend.model.CurrentAccount;
import org.sistemagestion.backend.model.Payment;
import org.sistemagestion.backend.model.Supplier;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class PaymentMapper {

    public Payment toPayment(PaymentRequestDto requestDto, CurrentAccount currentAccount) {

        return Payment.builder()
                .paymentType(requestDto.getPaymentType())
                .amount(requestDto.getAmount())
                .date(LocalDate.now())
                .currentAccount(currentAccount)
                .balance(currentAccount.getCurrentBalance() - requestDto.getAmount())
                .build();
    }

    public PaymentResponseDto toDto(Payment payment) {

        Supplier supplier = payment.getSupplier();

        return PaymentResponseDto.builder()
                .id(payment.getId())
                .currentAccountId(payment.getCurrentAccount().getId())
                .date(payment.getDate())
                .paymentType(payment.getPaymentType())
                .amount(payment.getAmount())
                .mirror(payment.getOriginPayment() != null)
                .supplierId(supplier != null ? supplier.getId() : null)
                .supplierName(supplier != null ? supplier.getCompanyName() : null)
                .build();
    }

}
