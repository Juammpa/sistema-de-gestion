package org.sistemagestion.backend.service.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sistemagestion.backend.dto.payment.PaymentRequestDto;
import org.sistemagestion.backend.dto.payment.PaymentResponseDto;
import org.sistemagestion.backend.exception.ResourceNotFoundException;
import org.sistemagestion.backend.mapper.PaymentMapper;
import org.sistemagestion.backend.model.ButcherShop;
import org.sistemagestion.backend.model.CurrentAccount;
import org.sistemagestion.backend.model.Payment;
import org.sistemagestion.backend.model.Supplier;
import org.sistemagestion.backend.repository.ButcherShopRepository;
import org.sistemagestion.backend.repository.CurrentAccountRepository;
import org.sistemagestion.backend.repository.PaymentRepository;
import org.sistemagestion.backend.service.cash.CashService;
import org.sistemagestion.backend.service.movement.BalanceRecalculationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService implements IPaymentService{

    private final PaymentRepository paymentRepository;
    private final CurrentAccountRepository currentAccountRepository;
    private final ButcherShopRepository butcherShopRepository;
    private final PaymentMirrorService paymentMirrorService;
    private final BalanceRecalculationService balanceRecalculationService;
    private final CashService cashService;
    private final PaymentMapper mapper;

    @Override
    @Transactional
    public PaymentResponseDto createPayment(PaymentRequestDto request) {

       CurrentAccount currentAccount = currentAccountRepository.findById(request.getCurrentAccountId())
               .orElseThrow(() -> new ResourceNotFoundException("CurrentAccount","id",request.getCurrentAccountId()));

       Supplier supplier = paymentMirrorService.resolveSupplier(request.getPaymentType(), request.getSupplierId(), currentAccount);
       cashService.assertSufficientBalance(currentAccount, request.getPaymentType(), request.getAmount(), null);

       Payment payment = mapper.toPayment(request, currentAccount);
       payment.setSupplier(supplier);
       Payment paymentSaved = paymentRepository.save(payment);

       paymentMirrorService.sync(paymentSaved);
       balanceRecalculationService.recalculate(currentAccount);

       log.info("Pago creado correctamente. ID: {}", paymentSaved.getId());
       return mapper.toDto(paymentSaved);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponseDto getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .map(payment -> mapper.toDto(payment,findOriginShop(payment)))
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));
    }

    @Override
    @Transactional
    public PaymentResponseDto updatePayment(Long id, PaymentRequestDto request) {

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));

        paymentMirrorService.assertEditable(payment);
        Supplier supplier = paymentMirrorService.resolveSupplier(request.getPaymentType(), request.getSupplierId(), payment.getCurrentAccount());
        cashService.assertSufficientBalance(payment.getCurrentAccount(), request.getPaymentType(), request.getAmount(), payment);

        payment.setDate(request.getDate());
        payment.setPaymentType(request.getPaymentType());
        payment.setAmount(request.getAmount());
        payment.setSupplier(supplier);

        Payment paymentUpdated = paymentRepository.saveAndFlush(payment);

        paymentMirrorService.sync(paymentUpdated);
        balanceRecalculationService.recalculate(payment.getCurrentAccount());

        log.info("Pago actualizado correctamente. ID: {}", payment.getId());
        return mapper.toDto(paymentUpdated);
    }

    @Override
    @Transactional
    public void deletePaymentById(Long id) {

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment","id", id));

        paymentMirrorService.assertEditable(payment);

        // Actualizamos el balance de la cuenta
        CurrentAccount currentAccount = payment.getCurrentAccount();
        paymentMirrorService.deleteMirrorOf(payment);
        paymentRepository.delete(payment);
        paymentRepository.flush();

        balanceRecalculationService.recalculate(currentAccount);

        log.info("Pago eliminado corectamente. ID: {}", payment.getId());
    }

    // Sucursal del cliente que hizo el pago original (null si el pago no es espejo)
    private ButcherShop findOriginShop(Payment payment) {
        Payment origin = payment.getOriginPayment();
        if (origin == null) return null;

        return butcherShopRepository.findByCurrentAccountId(origin.getCurrentAccount().getId())
                .orElse(null);
    }
}
