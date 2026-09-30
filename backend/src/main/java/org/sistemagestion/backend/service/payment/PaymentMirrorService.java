package org.sistemagestion.backend.service.payment;

import lombok.RequiredArgsConstructor;
import org.sistemagestion.backend.exception.BusinessRuleException;
import org.sistemagestion.backend.exception.ResourceNotFoundException;
import org.sistemagestion.backend.model.CurrentAccount;
import org.sistemagestion.backend.model.Payment;
import org.sistemagestion.backend.model.Supplier;
import org.sistemagestion.backend.model.enums.PaymentType;
import org.sistemagestion.backend.repository.ButcherShopRepository;
import org.sistemagestion.backend.repository.PaymentRepository;
import org.sistemagestion.backend.repository.SupplierRepository;
import org.sistemagestion.backend.service.movement.BalanceRecalculationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentMirrorService {

    private final PaymentRepository paymentRepository;
    private final SupplierRepository supplierRepository;
    private final ButcherShopRepository butcherShopRepository;
    private final BalanceRecalculationService balanceRecalculationService;

    // Un pago espejo no se modifica directamente: se edita desde el pago original
    public void assertEditable(Payment payment) {
        if (payment.getOriginPayment() != null) {
            throw new BusinessRuleException(
                    "Este pago se generó automáticamente a partir de un pago de cliente y no se puede modificar directamente."
            );
        }
    }

    // Valida las reglas de SUPPLIER_TRANSFER y devuelve el proveedor a asociar (null si el tipo no lo requiere)
    public Supplier resolveSupplier(PaymentType type, Long supplierId, CurrentAccount account) {

        if (type != PaymentType.SUPPLIER_TRANSFER) {
            return null;
        }

        if(!butcherShopRepository.existsByCurrentAccountId(account.getId())) {
            throw new BusinessRuleException("Los pagos a proveedor solo se pueden registrar desde la cuenta de un cliente.");
        }

        if(supplierId == null) {
            throw new BusinessRuleException("Indicá el proveedor al que se le realiza el pago.");
        }

        return supplierRepository.findByIdAndIsActiveTrue(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier","id", supplierId));

    }

    // Deja el espejo acorde al estado actual del pago original (crear, actualizar, mover o eliminar)
    @Transactional
    public void sync(Payment payment) {

        Optional<Payment> existingMirror =
                paymentRepository.findByOriginPaymentId(payment.getId());

        if (payment.getPaymentType() != PaymentType.SUPPLIER_TRANSFER) {
            existingMirror.ifPresent(this::remove);
            return;
        }

        CurrentAccount targetAccount = payment.getSupplier().getCurrentAccount();
        Payment mirror = existingMirror.orElseGet(() -> newMirror(payment, targetAccount));
        CurrentAccount previousAccount = mirror.getCurrentAccount();

        mirror.setCurrentAccount(targetAccount);
        mirror.setDate(payment.getDate());
        mirror.setAmount(payment.getAmount());
        paymentRepository.saveAndFlush(mirror);

        balanceRecalculationService.recalculate(targetAccount);

        // Si el cliente cambio de proveedor, hay que recalcular tambien la cuenta anterior
        if(!previousAccount.getId().equals(targetAccount.getId())) {
            balanceRecalculationService.recalculate(previousAccount);
        }
    }

    @Transactional
    public void deleteMirrorOf(Payment payment) {
        paymentRepository.findByOriginPaymentId(payment.getId()).ifPresent(this::remove);
    }

    private void remove(Payment mirror) {
        CurrentAccount account = mirror.getCurrentAccount();
        paymentRepository.delete(mirror);
        paymentRepository.flush();
        balanceRecalculationService.recalculate(account);
    }

    private Payment newMirror(Payment origin, CurrentAccount supplierAccount) {
        return Payment.builder()
                .paymentType(PaymentType.TRANSFER)
                .originPayment(origin)
                .currentAccount(supplierAccount)
                .build();
    }
}
