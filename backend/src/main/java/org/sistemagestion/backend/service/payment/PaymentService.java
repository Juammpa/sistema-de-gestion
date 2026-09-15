package org.sistemagestion.backend.service.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sistemagestion.backend.dto.payment.PaymentRequestDto;
import org.sistemagestion.backend.dto.payment.PaymentResponseDto;
import org.sistemagestion.backend.exception.ResourceNotFoundException;
import org.sistemagestion.backend.mapper.PaymentMapper;
import org.sistemagestion.backend.model.CurrentAccount;
import org.sistemagestion.backend.model.Payment;
import org.sistemagestion.backend.repository.ButcherShopRepository;
import org.sistemagestion.backend.repository.CurrentAccountRepository;
import org.sistemagestion.backend.repository.PaymentRepository;
import org.sistemagestion.backend.service.movement.BalanceRecalculationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService implements IPaymentService{

    private final PaymentRepository paymentRepository;
    private final CurrentAccountRepository currentAccountRepository;
    private final BalanceRecalculationService balanceRecalculationService;
    private final PaymentMapper mapper;

    @Override
    @Transactional
    public PaymentResponseDto createPayment(PaymentRequestDto request) {

       CurrentAccount currentAccount = currentAccountRepository.findById(request.getCurrentAccountId())
               .orElseThrow(() -> new ResourceNotFoundException("CurrentAccount","id",request.getCurrentAccountId()));

       Payment payment = mapper.toPayment(request, currentAccount);
       Payment paymentSaved = paymentRepository.save(payment);

       balanceRecalculationService.recalculate(currentAccount);

       log.info("Pago creado correctamente. ID: {}", paymentSaved.getId());
       return mapper.toDto(paymentSaved);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponseDto getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .map(mapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));
    }

    @Override
    @Transactional
    public PaymentResponseDto updatePayment(Long id, PaymentRequestDto request) {

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));

        payment.setPaymentType(request.getPaymentType());
        payment.setAmount(request.getAmount());

        Payment paymentUpdated = paymentRepository.saveAndFlush(payment);

        balanceRecalculationService.recalculate(payment.getCurrentAccount());

        log.info("Pago actualizado correctamente. ID: {}", payment.getId());
        return mapper.toDto(paymentUpdated);
    }

    @Override
    @Transactional
    public void deletePaymentById(Long id) {

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment","id", id));

        // Actualizamos el balance de la cuenta
        CurrentAccount currentAccount = payment.getCurrentAccount();
        paymentRepository.delete(payment);
        paymentRepository.flush();

        balanceRecalculationService.recalculate(currentAccount);

        log.info("Pago eliminado corectamente. ID: {}", payment.getId());
    }
}
