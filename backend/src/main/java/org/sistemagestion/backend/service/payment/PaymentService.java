package org.sistemagestion.backend.service.payment;

import lombok.RequiredArgsConstructor;
import org.sistemagestion.backend.common.Decimals;
import lombok.extern.slf4j.Slf4j;
import org.sistemagestion.backend.dto.payment.PaymentRequestDto;
import org.sistemagestion.backend.dto.payment.PaymentResponseDto;
import org.sistemagestion.backend.exception.BusinessRuleException;
import org.sistemagestion.backend.exception.ResourceNotFoundException;
import org.sistemagestion.backend.mapper.PaymentMapper;
import org.sistemagestion.backend.model.*;
import org.sistemagestion.backend.model.enums.PaymentType;
import org.sistemagestion.backend.repository.ButcherShopRepository;
import org.sistemagestion.backend.repository.CurrentAccountRepository;
import org.sistemagestion.backend.repository.CustomPaymentMethodRepository;
import org.sistemagestion.backend.repository.PaymentRepository;
import org.sistemagestion.backend.service.account.AccountGuard;
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
    private final CustomPaymentMethodRepository customPaymentMethodRepository;
    private final BalanceRecalculationService balanceRecalculationService;
    private final CashService cashService;
    private final AccountGuard accountGuard;
    private final PaymentMapper mapper;

    @Override
    @Transactional
    public PaymentResponseDto createPayment(PaymentRequestDto request) {

        assertManualType(request.getPaymentType());

       CurrentAccount currentAccount = currentAccountRepository.findById(request.getCurrentAccountId())
               .orElseThrow(() -> new ResourceNotFoundException("CurrentAccount","id",request.getCurrentAccountId()));

       accountGuard.assertOwnerActive(currentAccount);

       Supplier supplier = paymentMirrorService.resolveSupplier(request.getPaymentType(), request.getSupplierId(), currentAccount);
       CustomPaymentMethod customMethod = resolveCustomMethod(request, null);
       cashService.assertSufficientBalance(currentAccount,  request.getAmount(), null);

       Payment payment = mapper.toPayment(request, currentAccount);
       payment.setSupplier(supplier);
       payment.setCustomPaymentMethod(customMethod);
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
                .map(payment -> {
                    PaymentResponseDto dto = mapper.toDto(payment, findOriginShop(payment));
                    dto.setRejected(paymentRepository.existsByRejectedPaymentId(payment.getId()));
                    return dto;
                }).orElseThrow(() -> new ResourceNotFoundException("Payment","id",id));

    }

    @Override
    @Transactional
    public PaymentResponseDto updatePayment(Long id, PaymentRequestDto request) {

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));

        accountGuard.assertOwnerActive(payment.getCurrentAccount());
        paymentMirrorService.assertEditable(payment);
        assertManualType(request.getPaymentType());
        assertNotRejected(payment);

        Supplier supplier = paymentMirrorService.resolveSupplier(request.getPaymentType(), request.getSupplierId(), payment.getCurrentAccount());
        CustomPaymentMethod customMethod = resolveCustomMethod(request, payment.getCustomPaymentMethod());
        cashService.assertSufficientBalance(payment.getCurrentAccount(), request.getAmount(), payment);

        payment.setDate(request.getDate());
        payment.setPaymentType(request.getPaymentType());
        payment.setAmount(Decimals.money(request.getAmount()));
        payment.setSupplier(supplier);
        payment.setCustomPaymentMethod(customMethod);

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

        // El cheque rechazado no se borra, pero el rechazo si (es la forma de deshacerlo)
        if (payment.getPaymentType() != PaymentType.CHECK_REJECTION) {
            assertNotRejected(payment);
        }

        paymentMirrorService.assertEditable(payment);

        // Actualizamos el balance de la cuenta
        CurrentAccount currentAccount = payment.getCurrentAccount();
        paymentMirrorService.deleteMirrorOf(payment);
        paymentRepository.delete(payment);
        paymentRepository.flush();

        balanceRecalculationService.recalculate(currentAccount);

        log.info("Pago eliminado corectamente. ID: {}", payment.getId());
    }

    @Override
    @Transactional
    public PaymentResponseDto rejectCheck(Long id) {

        Payment check = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));
        CurrentAccount account = check.getCurrentAccount();

        accountGuard.assertOwnerActive(account);

        if (check.getPaymentType() != PaymentType.CHECK) {
            throw new BusinessRuleException("Solo se puede rechazar un pago realizado con cheque.");
        }

        if (!butcherShopRepository.existsByCurrentAccountId(account.getId())) {
            throw new BusinessRuleException("Solo se pueden rechazar cheques de clientes.");
        }

        if (paymentRepository.existsByRejectedPaymentId(id)) {
            throw new BusinessRuleException("Este cheque ya fue rechazado.");
        }

        Payment rejection = paymentRepository.save(mapper.toCheckRejection(check));
        balanceRecalculationService.recalculate(account);

        log.info("Cheque rechazado correctamente. Cheque ID: {}, rechazo ID: {}", id, rejection.getId());
        return mapper.toDto(rejection);
    }

    // Sucursal del cliente que hizo el pago original (null si el pago no es espejo)
    private ButcherShop findOriginShop(Payment payment) {
        Payment origin = payment.getOriginPayment();
        if (origin == null) return null;

        return butcherShopRepository.findByCurrentAccountId(origin.getCurrentAccount().getId())
                .orElse(null);
    }

    // Metodo personalizado a asociar (null si el tipo no es CUSTOM)
    // Se puede conservar el metodo actual del pago aunque ya este eliminado, pero no elegir uno eliminado
    private CustomPaymentMethod resolveCustomMethod(PaymentRequestDto request, CustomPaymentMethod current) {

        if (request.getPaymentType() != PaymentType.CUSTOM) {
            return null;
        }

        Long methodId = request.getCustomPaymentMethodId();

        if (methodId == null) {
            throw new BusinessRuleException("Indicá el método de pago.");
        }

        if (current != null && current.getId().equals(methodId)) {
            return current;
        }

        return customPaymentMethodRepository.findByIdAndIsActiveTrue(methodId)
                .orElseThrow(() -> new ResourceNotFoundException("CustomPaymentMethod","id",methodId));

    }

    // CHECK_REJECTION solo lo genera el sistema al rechazar un cheque, nunca se carga a mano
    private void assertManualType(PaymentType type) {
        if (type == PaymentType.CHECK_REJECTION) {
            throw new BusinessRuleException("Para rechazar un cheque usá la opción \"Rechazar cheque\" del pago.");
        }
    }

    // Un cheque rechazado o su rechazo no se modifican: para corregir, se elimina el rechazo
    private void assertNotRejected(Payment payment) {
        if (payment.getPaymentType() == PaymentType.CHECK_REJECTION) {
            throw new BusinessRuleException("Un rechazo de cheque no se puede modificar. Eliminalo si fue un error.");
        }

        if (paymentRepository.existsByRejectedPaymentId(payment.getId())) {
            throw new BusinessRuleException("Este cheque fue rechazado. Eliminá primero el rechazo para modificarlo o borrarlo.");
        }
    }


}
