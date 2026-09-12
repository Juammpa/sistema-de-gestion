package org.sistemagestion.backend.mapper;

import org.sistemagestion.backend.dto.payment.PaymentRequestDto;
import org.sistemagestion.backend.dto.payment.PaymentResponseDto;
import org.sistemagestion.backend.model.Client;
import org.sistemagestion.backend.model.CurrentAccount;
import org.sistemagestion.backend.model.Payment;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class PaymentMapper {

    public Payment toPayment(PaymentRequestDto requestDto, Client client, CurrentAccount currentAccount) {

        return Payment.builder()
                .paymentType(requestDto.getPaymentType())
                .client(client)
                .amount(requestDto.getAmount())
                .date(LocalDate.now())
                .currentAccount(currentAccount)
                .balance(currentAccount.getCurrentBalance() - requestDto.getAmount())
                .build();
    }

    public PaymentResponseDto toDto(Payment payment) {
        return PaymentResponseDto.builder()
                .id(payment.getId())
                .clientId(payment.getClient().getId())
                .date(payment.getDate())
                .paymentType(payment.getPaymentType())
                .amount(payment.getAmount())
                .build();
    }

}
