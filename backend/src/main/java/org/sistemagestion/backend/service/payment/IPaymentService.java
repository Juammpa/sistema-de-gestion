package org.sistemagestion.backend.service.payment;

import org.sistemagestion.backend.dto.payment.PaymentRequestDto;
import org.sistemagestion.backend.dto.payment.PaymentResponseDto;

import java.util.List;

public interface IPaymentService {

    PaymentResponseDto createPayment(PaymentRequestDto request);
    PaymentResponseDto getPaymentById(Long id);
    List<PaymentResponseDto> getAllPaymentsByClient(Long clientId);
    PaymentResponseDto updatePayment(Long id, PaymentRequestDto request);
    void deletePaymentById(Long id);

}
