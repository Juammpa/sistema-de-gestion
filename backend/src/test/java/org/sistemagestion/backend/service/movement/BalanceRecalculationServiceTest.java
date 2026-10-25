package org.sistemagestion.backend.service.movement;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sistemagestion.backend.model.CurrentAccount;
import org.sistemagestion.backend.model.Movement;
import org.sistemagestion.backend.model.Payment;
import org.sistemagestion.backend.model.Sale;
import org.sistemagestion.backend.model.enums.PaymentType;
import org.sistemagestion.backend.repository.MovementRepository;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("BalanceRecalcultationService")
class BalanceRecalculationServiceTest {

    @Mock private MovementRepository movementRepository;

    @InjectMocks
    private BalanceRecalculationService service;

    // ====== Helpers ======

    private CurrentAccount account() {
        CurrentAccount account = new CurrentAccount();
        account.setId(1L);
        return account;
    }

    private Sale sale(long id, String amount) {
        return Sale.builder().id(id).amount(new BigDecimal(amount)).build();
    }

    private Payment payment(long id, PaymentType type, String amount) {
        return Payment.builder().id(id).paymentType(type).amount(new BigDecimal(amount)).build();
    }

    @Nested
    @DisplayName("recalculate()")
    class RecalculateTests {

        @Test
        @DisplayName("Un pago resta del saldo de la cuenta")
        void recalculateSubtractsPayments() {

            // Arrange
            CurrentAccount account = account();
            Sale sale = sale(1L, "10000.00");
            Payment check = payment(2L, PaymentType.CHECK, "5000.00");
            List<Movement> movements = List.of(sale, check);

            when(movementRepository.findByCurrentAccountIdOrderByDateAscIdAsc(1L))
                    .thenReturn(movements);

            // Act
            service.recalculate(account);

            // Assert
            assertThat(sale.getBalance()).isEqualByComparingTo("10000.00");
            assertThat(check.getBalance()).isEqualByComparingTo("5000.00");
            assertThat(account.getCurrentBalance()).isEqualByComparingTo("5000.00");
        }

        @Test
        @DisplayName("El rechazo de un cheque suma a la deuda en lugar de restar")
        void recalculateAddsCheckRejections() {

            // Arrange
            CurrentAccount account = account();
            Sale sale = sale(1L, "10000.00");
            Payment check = payment(2L, PaymentType.CHECK, "5000.00");
            Payment rejection = payment(3L, PaymentType.CHECK_REJECTION, "5000.00");
            List<Movement> movements = List.of(sale, check, rejection);

            when(movementRepository.findByCurrentAccountIdOrderByDateAscIdAsc(1L))
                    .thenReturn(movements);

            // Act
            service.recalculate(account);

            // Assert: el rechazo deja la deuda como estaba antes
            assertThat(check.getBalance()).isEqualByComparingTo("5000.00");
            assertThat(rejection.getBalance()).isEqualByComparingTo("10000.00");
            assertThat(account.getCurrentBalance()).isEqualByComparingTo("10000.00");
        }


    }



}