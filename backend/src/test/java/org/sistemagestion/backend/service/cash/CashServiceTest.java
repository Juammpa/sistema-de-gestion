package org.sistemagestion.backend.service.cash;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sistemagestion.backend.dto.cash.CashDayDto;
import org.sistemagestion.backend.dto.cash.CashPaymentDto;
import org.sistemagestion.backend.exception.BusinessRuleException;
import org.sistemagestion.backend.model.CurrentAccount;
import org.sistemagestion.backend.model.Payment;
import org.sistemagestion.backend.model.enums.PaymentType;
import org.sistemagestion.backend.repository.CashRepository;
import org.sistemagestion.backend.repository.SupplierRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
@DisplayName("CashService")
class CashServiceTest {

    @Mock private CashRepository cashRepository;
    @Mock private SupplierRepository supplierRepository;

    @InjectMocks
    private CashService service;

    // ====== Helpers ======

    private CashPaymentDto clientPayment(long id, LocalDate date, PaymentType type, String amount) {
        return new CashPaymentDto(id, date, type, new BigDecimal(amount), "Castella Julio", "Carniceria Prueba", null, null);
    }

    // La cuenta arranca con $1.000 y sin extracciones a proveedores
    private void givenOpeningBalance(LocalDate start) {

        when(cashRepository.sumClientPaymentsBefore(PaymentType.SUPPLIER_TRANSFER, start))
                .thenReturn(new BigDecimal("1000.00"));
        when(cashRepository.sumSupplierPaymentsBefore(start))
                .thenReturn(BigDecimal.ZERO);
    }


    @Nested
    @DisplayName("getCashDays()")
    class GetCashDaysTests {

        @Test
        @DisplayName("Un cheque y su rechazo el mismo dia dejan la caja sin cambios")
        void getCashDaysNetsCheckAndRejectionOnTheSameDay() {

            // Arrange
            LocalDate day = LocalDate.of(2026, 10, 25);
            givenOpeningBalance(day);
            when(cashRepository.findClientPayments(day, day)).thenReturn(List.of(clientPayment(1L, day, PaymentType.CHECK, "500.00"),clientPayment(2L, day, PaymentType.CHECK_REJECTION, "500.00")));

            // Act
            List<CashDayDto> days = service.getCashDays(day, day);

            // Assert
            assertThat(days).hasSize(1);
            assertThat(days.get(0).total()).isEqualByComparingTo("0");
            assertThat(days.get(0).cash()).isEqualByComparingTo("0");
            assertThat(days.get(0).closingBalance()).isEqualByComparingTo("1000.00");
        }

        @Test
        @DisplayName("El rechazo resta de la caja el dia en que ocurre, sin tocar el dia del cobro")
        void getCashDaysSubtractsRejectionOnItsOwnDay() {

            // Arrange
            LocalDate collectedOn = LocalDate.of(2026, 10, 24);
            LocalDate rejectedOn = LocalDate.of(2026, 10, 25);
            givenOpeningBalance(collectedOn);
            when(cashRepository.findClientPayments(collectedOn, rejectedOn)).thenReturn(List.of(
                    clientPayment(1L, collectedOn, PaymentType.CHECK, "500.00"),
                    clientPayment(2L, rejectedOn, PaymentType.CHECK_REJECTION, "500.00")
            ));
            when(cashRepository.findSupplierPayments(collectedOn, rejectedOn)).thenReturn(List.of());

            // Act
            List<CashDayDto> days = service.getCashDays(collectedOn, rejectedOn);

            // Assert: los dias vienen del mas reciente al mas antiguo
            CashDayDto rejectionDay = days.get(0);
            CashDayDto collectionDay = days.get(1);

            assertThat(collectionDay.cash()).isEqualByComparingTo("500.00");
            assertThat(collectionDay.closingBalance()).isEqualByComparingTo("1500.00");

            assertThat(rejectionDay.cash()).isEqualByComparingTo("-500.00");
            assertThat(rejectionDay.closingBalance()).isEqualByComparingTo("1000.00");
        }

    }

    @Nested
    @DisplayName("assertSufficientBalance()")
    class AssertSufficientBalanceTests {

        // ====== Helpers ======
        private CurrentAccount supplierAccount() {
            CurrentAccount account = new CurrentAccount();
            account.setId(7L);
            return account;
        }

        private void givenCashOf10000() {

            when(cashRepository.sumClientPayments(PaymentType.SUPPLIER_TRANSFER))
                    .thenReturn(new BigDecimal("10000.00"));
            when(cashRepository.sumSupplierPayments()).thenReturn(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Rechaza un pago a un proveedor mayor a la caja, sin importar el metodo")
        void assertSufficientBalanceRejectsAmountAboveCash() {

            // Arrange
            CurrentAccount account = supplierAccount();
            when(supplierRepository.existsByCurrentAccountId(7L)).thenReturn(true);
            givenCashOf10000();

            // Act + Assert
            assertThatThrownBy(() -> service.assertSufficientBalance(account, new BigDecimal("10000.01"), null))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("Saldo de caja insuficiente");

        }

        @Test
        @DisplayName("Permite pagar exactamente lo que hay en la caja")
        void assertSufficientBalanceAllowsTheFullCash() {

            // Arrange
            CurrentAccount account = supplierAccount();
            when(supplierRepository.existsByCurrentAccountId(7L)).thenReturn(true);
            givenCashOf10000();

            // Act + Assert
            assertThatCode(() -> service.assertSufficientBalance(account, new BigDecimal("10000.00"), null))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Al editar, el pago que se modifica no se descuenta dos veces")
        void assertSufficientBalanceAddsBackTheEditedPayment() {

            // Arrange: ya salieron $4.000 (el pago que se edita), asi que la caja marca $6.000
            CurrentAccount account = supplierAccount();
            Payment existing = Payment.builder().amount(new BigDecimal("4000.00")).build();

            when(supplierRepository.existsByCurrentAccountId(7L)).thenReturn(true);
            when(cashRepository.sumClientPayments(PaymentType.SUPPLIER_TRANSFER)).thenReturn(new BigDecimal("10000.00"));
            when(cashRepository.sumSupplierPayments()).thenReturn(new BigDecimal("4000.00"));

            // Act + Assert: puede subirlo hasta los $10.000 originales
            assertThatCode(() -> service.assertSufficientBalance(account, new BigDecimal("10000.00"), existing))
                    .doesNotThrowAnyException();

        }

        @Test
        @DisplayName("No valida la caja en la cuenta de un cliente")
        void assertSufficientBalanceIgnoresNonSupplierAccounts() {

            // Arrange
            CurrentAccount account = supplierAccount();
            when(supplierRepository.existsByCurrentAccountId(7L)).thenReturn(false);

            // Act + Assert
            assertThatCode(() -> service.assertSufficientBalance(account, new BigDecimal("999999.00"),null))
                    .doesNotThrowAnyException();

            verifyNoInteractions(cashRepository);
        }
    }


}