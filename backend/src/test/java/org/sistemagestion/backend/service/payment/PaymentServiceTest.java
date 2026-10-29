package org.sistemagestion.backend.service.payment;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sistemagestion.backend.dto.payment.PaymentRequestDto;
import org.sistemagestion.backend.dto.payment.PaymentResponseDto;
import org.sistemagestion.backend.exception.BusinessRuleException;
import org.sistemagestion.backend.exception.ResourceNotFoundException;
import org.sistemagestion.backend.mapper.PaymentMapper;
import org.sistemagestion.backend.model.CurrentAccount;
import org.sistemagestion.backend.model.CustomPaymentMethod;
import org.sistemagestion.backend.model.Payment;
import org.sistemagestion.backend.model.enums.PaymentType;
import org.sistemagestion.backend.repository.ButcherShopRepository;
import org.sistemagestion.backend.repository.CurrentAccountRepository;
import org.sistemagestion.backend.repository.CustomPaymentMethodRepository;
import org.sistemagestion.backend.repository.PaymentRepository;
import org.sistemagestion.backend.service.account.AccountGuard;
import org.sistemagestion.backend.service.cash.CashService;
import org.sistemagestion.backend.service.movement.BalanceRecalculationService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService")
class PaymentServiceTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private CurrentAccountRepository currentAccountRepository;
    @Mock private ButcherShopRepository butcherShopRepository;
    @Mock private PaymentMirrorService paymentMirrorService;
    @Mock private CustomPaymentMethodRepository customPaymentMethodRepository;
    @Mock private BalanceRecalculationService balanceRecalculationService;
    @Mock private CashService cashService;
    @Mock private AccountGuard accountGuard;

    // El mapper es real
    @Spy private PaymentMapper mapper = new PaymentMapper();

    @InjectMocks
    private PaymentService service;

    // ====== Helpers ======

    private CurrentAccount account() {
        CurrentAccount account = new CurrentAccount();
        account.setId(1L);
        account.setCurrentBalance(new BigDecimal("5000.00"));
        return account;
    }

    private Payment payment(long id, PaymentType type, CurrentAccount account) {
        return Payment.builder()
                .id(id)
                .paymentType(type)
                .amount(new BigDecimal("5000.00"))
                .date(LocalDate.of(2026, 10, 24))
                .currentAccount(account)
                .build();
    }

    private PaymentRequestDto request(PaymentType type) {
        return PaymentRequestDto.builder()
                .paymentType(type)
                .amount(new BigDecimal("5000.00"))
                .currentAccountId(1L)
                .date(LocalDate.of(2026, 10, 24))
                .build();
    }

    private CustomPaymentMethod customMethod(long id, boolean active) {
        return CustomPaymentMethod.builder()
                .id(id)
                .name("Mercado Pago")
                .isActive(active)
                .build();
    }

    private PaymentRequestDto customRequest(PaymentType type, Long methodId) {
        PaymentRequestDto request = request(type);
        request.setCustomPaymentMethodId(methodId);
        return request;
    }

    @Nested
    @DisplayName("createPayment()")
    class CreatePaymentTests {

        @Test
        @DisplayName("No permite cargar a mano un rechazo de cheque")
        void createPaymentShouldRejectManualCheckRejection() {

            // Act + Assert
            assertThatThrownBy(() -> service.createPayment(request(PaymentType.CHECK_REJECTION)))
                    .isInstanceOf(BusinessRuleException.class);

            verify(paymentRepository, never()).save(any());
        }

    }

    @Nested
    @DisplayName("updatePayment()")
    class UpdatePaymentTests {

        @Test
        @DisplayName("No permite convertir un pago en un rechazo de cheque")
        void updatePaymentShouldRejectSwitchingToCheckRejection() {

            // Arrange
            Payment check = payment(10L, PaymentType.CHECK, account());
            when(paymentRepository.findById(10L)).thenReturn(Optional.of(check));

            // Act + Assert
            assertThatThrownBy(() -> service.updatePayment(10L, request(PaymentType.CHECK_REJECTION)))
                    .isInstanceOf(BusinessRuleException.class);

            verify(paymentRepository, never()).save(any());
        }

        @Test
        @DisplayName("No permite editar un cheque que ya fue rechazado")
        void updatePaymentShouldRejectEditingARejectedCheck() {

            // Arrange
            Payment check = payment(10L, PaymentType.CHECK, account());
            when(paymentRepository.findById(10L)).thenReturn(Optional.of(check));
            when(paymentRepository.existsByRejectedPaymentId(10L)).thenReturn(true);

            // Act + Assert
            assertThatThrownBy(() -> service.updatePayment(10L, request(PaymentType.CHECK)))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("rechazado");

            verify(paymentRepository, never()).save(any());
        }

        @Test
        @DisplayName("No permite editar el rechazo de un cheque")
        void updatePaymentShouldRejectEditingARejection() {

            // Arrange
            Payment rejection = payment(11L, PaymentType.CHECK_REJECTION, account());
            when(paymentRepository.findById(11L)).thenReturn(Optional.of(rejection));

            // Act + Assert
            assertThatThrownBy(() -> service.updatePayment(11L, request(PaymentType.CASH)))
                    .isInstanceOf(BusinessRuleException.class);

            verify(paymentRepository, never()).save(any());
        }

    }

    @Nested
    @DisplayName("deletePaymentById()")
    class DeletePaymentTests {

        @Test
        @DisplayName("No permite borrar un cheque que ya fue rechazado")
        void deletePaymentShouldRejectDeletingARejectedCheck() {

            // Arrange
            Payment check = payment(10L, PaymentType.CHECK, account());
            when(paymentRepository.findById(10L)).thenReturn(Optional.of(check));
            when(paymentRepository.existsByRejectedPaymentId(10L)).thenReturn(true);

            // Act + Assert
            assertThatThrownBy(() -> service.deletePaymentById(10L))
                    .isInstanceOf(BusinessRuleException.class);

            verify(paymentRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Permite borrar el rechazo (es la forma de deshacerlo) y recalcula el saldo")
        void deletePaymentShouldAllowDeletingARejection() {

            // Arrange
            CurrentAccount account = account();
            Payment rejection = payment(11L, PaymentType.CHECK_REJECTION, account);
            when(paymentRepository.findById(11L)).thenReturn(Optional.of(rejection));

            // Act
            service.deletePaymentById(11L);

            // Assert
            verify(paymentRepository).delete(rejection);
            verify(balanceRecalculationService).recalculate(account);
        }

        @Test
        @DisplayName("Permite borrar un cheque que no fue rechazado")
        void deletePaymentShouldAllowDeletingANonRejectedCheck() {

            // Arrange
            CurrentAccount account = account();
            Payment check = payment(10L, PaymentType.CHECK, account);
            when(paymentRepository.findById(10L)).thenReturn(Optional.of(check));
            when(paymentRepository.existsByRejectedPaymentId(10L)).thenReturn(false);

            // Act
            service.deletePaymentById(10L);

            // Assert
            verify(paymentRepository).delete(check);
            verify(balanceRecalculationService).recalculate(account);
        }

    }

    @Nested
    @DisplayName("rejectCheck()")
    class RejectCheckTests {

        @Test
        @DisplayName("Registra un rechazo con el mismo monto, vinculado al cheque, y recalcula el saldo")
        void rejectCheckCreatesLinkedRejectionAndRecalculates() {

            // Arrange
            CurrentAccount account = account();
            Payment check = payment(10L, PaymentType.CHECK, account);

            when(paymentRepository.findById(10L)).thenReturn(Optional.of(check));
            when(butcherShopRepository.existsByCurrentAccountId(1L)).thenReturn(true);
            when(paymentRepository.existsByRejectedPaymentId(10L)).thenReturn(false);
            when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            PaymentResponseDto result = service.rejectCheck(10L);

            // Assert
            ArgumentCaptor<Payment> saved = ArgumentCaptor.forClass(Payment.class);
            verify(paymentRepository).save(saved.capture());
            Payment rejection = saved.getValue();

            assertThat(rejection.getPaymentType()).isEqualTo(PaymentType.CHECK_REJECTION);
            assertThat(rejection.getAmount()).isEqualByComparingTo("5000.00");
            assertThat(rejection.getRejectedPayment()).isSameAs(check);
            assertThat(rejection.getCurrentAccount()).isSameAs(account);
            assertThat(result.getPaymentType()).isEqualTo(PaymentType.CHECK_REJECTION);
            assertThat(result.getRejectedPaymentId()).isEqualTo(10L);
            verify(balanceRecalculationService).recalculate(account);
        }

        @Test
        @DisplayName("Debe fallar si el pago no existe")
        void rejectCheckShouldFailWhenPaymentNotFound() {

            // Arrange
            when(paymentRepository.findById(99L)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> service.rejectCheck(99L))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(paymentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe rechazar un pago que no es con cheque")
        void rejectCheckShouldRejectNonCheckPayments() {

            // Arrange
            Payment cash = payment(10L, PaymentType.CASH, account());
            when(paymentRepository.findById(10L)).thenReturn(Optional.of(cash));

            // Act + Assert
            assertThatThrownBy(() -> service.rejectCheck(10L))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("cheque");

            verify(paymentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe rechazar un cheque que no es de la cuenta de un cliente")
        void rejectCheckShouldRejectChecksOutsideClientAccount() {

            // Arrange
            Payment check = payment(10L, PaymentType.CHECK, account());
            when(paymentRepository.findById(10L)).thenReturn(Optional.of(check));
            when(butcherShopRepository.existsByCurrentAccountId(1L)).thenReturn(false);

            // Act + Assert
            assertThatThrownBy(() -> service.rejectCheck(10L))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("clientes");

            verify(paymentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe rechazar un cheque que ya fue rechazado")
        void rejectCheckShouldRejectAnAlreadyRejectedCheck() {

            // Arrange
            Payment check = payment(10L, PaymentType.CHECK, account());
            when(paymentRepository.findById(10L)).thenReturn(Optional.of(check));
            when(butcherShopRepository.existsByCurrentAccountId(1L)).thenReturn(true);
            when(paymentRepository.existsByRejectedPaymentId(10L)).thenReturn(true);

            // Act + Assert
            assertThatThrownBy(() -> service.rejectCheck(10L))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("ya fue rechazado");

            verify(paymentRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Metodo de pago personalizado")
    class CustomPaymentMethodTests {

        @Test
        @DisplayName("Al crear un pago CUSTOM lo asocia al metodo elegido")
        void createPaymentShouldAssociateTheCustomMethod() {

            // Arrange
            CustomPaymentMethod method = customMethod(7L, true);
            when(currentAccountRepository.findById(1L)).thenReturn(Optional.of(account()));
            when(customPaymentMethodRepository.findByIdAndIsActiveTrue(7L)).thenReturn(Optional.of(method));
            when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            PaymentResponseDto result = service.createPayment(customRequest(PaymentType.CUSTOM, 7L));

            // Assert
            ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
            verify(paymentRepository).save(captor.capture());
            assertThat(captor.getValue().getCustomPaymentMethod()).isSameAs(method);
            assertThat(result.getCustomPaymentMethodId()).isEqualTo(7L);
            assertThat(result.getCustomPaymentMethodName()).isEqualTo("Mercado Pago");
        }

        @Test
        @DisplayName("Rechaza un pago CUSTOM sin indicar el metodo")
        void createPaymentShouldRejectCustomWithoutMethod() {

            // Arrange
            when(currentAccountRepository.findById(1L)).thenReturn(Optional.of(account()));

            // Act + Assert
            assertThatThrownBy(() -> service.createPayment(customRequest(PaymentType.CUSTOM, null)))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("método de pago");

            verify(paymentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Rechaza un metodo que no existe, es de otro usuario o esta eliminado")
        void createPaymentShouldRejectUnknownOrArchivedMethod() {

            // Arrange: la consulta solo devuelve metodos activos del usuario
            when(currentAccountRepository.findById(1L)).thenReturn(Optional.of(account()));
            when(customPaymentMethodRepository.findByIdAndIsActiveTrue(7L)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> service.createPayment(customRequest(PaymentType.CUSTOM, 7L)))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(paymentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Ignora el metodo si el tipo de pago no es CUSTOM")
        void createPaymentShouldIgnoreTheMethodForOtherTypes() {

            // Arrange
            when(currentAccountRepository.findById(1L)).thenReturn(Optional.of(account()));
            when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            service.createPayment(customRequest(PaymentType.TRANSFER, 7L));

            // Assert: ni siquiera se consulta el metodo
            ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
            verify(paymentRepository).save(captor.capture());
            assertThat(captor.getValue().getCustomPaymentMethod()).isNull();
            verifyNoInteractions(customPaymentMethodRepository);
        }

        @Test
        @DisplayName("Al editar, conserva el metodo actual aunque ya este eliminado")
        void updatePaymentShouldKeepTheCurrentMethodEvenIfArchived() {

            // Arrange
            CustomPaymentMethod archived = customMethod(7L, false);
            Payment payment = payment(10L, PaymentType.CUSTOM, account());
            payment.setCustomPaymentMethod(archived);
            when(paymentRepository.findById(10L)).thenReturn(Optional.of(payment));
            when(paymentRepository.saveAndFlush(payment)).thenReturn(payment);

            // Act
            service.updatePayment(10L, customRequest(PaymentType.CUSTOM, 7L));

            // Assert: no hace falta buscarlo, ya lo tiene el pago
            assertThat(payment.getCustomPaymentMethod()).isSameAs(archived);
            verifyNoInteractions(customPaymentMethodRepository);
        }

        @Test
        @DisplayName("Al editar, permite cambiar a otro metodo activo")
        void updatePaymentShouldAllowSwitchingToAnotherActiveMethod() {

            // Arrange
            Payment payment = payment(10L, PaymentType.CUSTOM, account());
            payment.setCustomPaymentMethod(customMethod(7L, true));
            CustomPaymentMethod other = customMethod(8L, true);
            when(paymentRepository.findById(10L)).thenReturn(Optional.of(payment));
            when(customPaymentMethodRepository.findByIdAndIsActiveTrue(8L)).thenReturn(Optional.of(other));
            when(paymentRepository.saveAndFlush(payment)).thenReturn(payment);

            // Act
            service.updatePayment(10L, customRequest(PaymentType.CUSTOM, 8L));

            // Assert
            assertThat(payment.getCustomPaymentMethod()).isSameAs(other);
        }

        @Test
        @DisplayName("Al editar, rechaza cambiar a un metodo eliminado")
        void updatePaymentShouldRejectSwitchingToAnArchivedMethod() {

            // Arrange
            Payment payment = payment(10L, PaymentType.CUSTOM, account());
            payment.setCustomPaymentMethod(customMethod(7L, true));
            when(paymentRepository.findById(10L)).thenReturn(Optional.of(payment));
            when(customPaymentMethodRepository.findByIdAndIsActiveTrue(8L)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> service.updatePayment(10L, customRequest(PaymentType.CUSTOM, 8L)))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(paymentRepository, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("Al editar un CUSTOM a otro tipo, le saca el metodo")
        void updatePaymentShouldClearTheMethodWhenTypeChanges() {

            // Arrange
            Payment payment = payment(10L, PaymentType.CUSTOM, account());
            payment.setCustomPaymentMethod(customMethod(7L, true));
            when(paymentRepository.findById(10L)).thenReturn(Optional.of(payment));
            when(paymentRepository.saveAndFlush(payment)).thenReturn(payment);

            // Act
            service.updatePayment(10L, request(PaymentType.CASH));

            // Assert: sino la restriccion de la base rechazaria el pago
            assertThat(payment.getPaymentType()).isEqualTo(PaymentType.CASH);
            assertThat(payment.getCustomPaymentMethod()).isNull();
        }

    }


}