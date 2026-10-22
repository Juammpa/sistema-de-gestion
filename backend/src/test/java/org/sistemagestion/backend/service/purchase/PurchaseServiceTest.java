package org.sistemagestion.backend.service.purchase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sistemagestion.backend.dto.purchase.PurchaseRequestDto;
import org.sistemagestion.backend.exception.BusinessRuleException;
import org.sistemagestion.backend.exception.ResourceNotFoundException;
import org.sistemagestion.backend.mapper.PurchaseMapper;
import org.sistemagestion.backend.model.CurrentAccount;
import org.sistemagestion.backend.model.Purchase;
import org.sistemagestion.backend.model.PurchaseType;
import org.sistemagestion.backend.model.enums.Specie;
import org.sistemagestion.backend.repository.*;
import org.sistemagestion.backend.service.account.AccountGuard;
import org.sistemagestion.backend.service.movement.BalanceRecalculationService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PurchaseService")
class PurchaseServiceTest {

    @Mock private PurchaseRepository purchaseRepository;
    @Mock private PurchasePieceWeightRepository pieceWeightRepository;
    @Mock private CurrentAccountRepository currentAccountRepository;
    @Mock private SupplierRepository supplierRepository;
    @Mock private PurchaseTypeRepository purchaseTypeRepository;
    @Mock private BalanceRecalculationService balanceRecalculationService;
    @Mock private AccountGuard accountGuard;
    @Mock private PurchaseMapper purchaseMapper;

    @InjectMocks
    private PurchaseService service;

    // ====== Helpers ======

    private PurchaseType type(Long id, boolean active) {
        return PurchaseType.builder()
                .id(id)
                .name("Ternero")
                .specie(Specie.BOVINE)
                .isActive(active)
                .build();
    }

    @Nested
    @DisplayName("createPurchase()")
    class CreatePurchaseTests {

        @Test
        @DisplayName("Debe fallar si el tipo de compra no existe")
        void createPurchaseShouldFailWhenTypeNotFound() {

            // Arrange
            PurchaseRequestDto request = PurchaseRequestDto.builder()
                    .currentAccountId(1L)
                    .purchaseTypeId(99L)
                    .build();

            when(currentAccountRepository.findById(1L)).thenReturn(Optional.of(mock(CurrentAccount.class)));
            when(purchaseTypeRepository.findById(99L)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> service.createPurchase(request))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(purchaseRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe rechazar un tipo de compra archivado")
        void createPurchaseShouldRejectArchivedType() {

            // Arrange
            PurchaseRequestDto request = PurchaseRequestDto.builder()
                    .currentAccountId(1L)
                    .purchaseTypeId(5L)
                    .build();

            when(currentAccountRepository.findById(1L)).thenReturn(Optional.of(mock(CurrentAccount.class)));
            when(purchaseTypeRepository.findById(5L)).thenReturn(Optional.of(type(5L, false)));

            // Act + Assert
            assertThatThrownBy(() -> service.createPurchase(request))
                    .isInstanceOf(BusinessRuleException.class);

            verify(purchaseRepository, never()).save(any());
        }

    }

    @Nested
    @DisplayName("updatePurchase()")
    class UpdatePurchaseTests {

        @Test
        @DisplayName("Debe permitir editar una compra que conserva su tipo aunque este archivado")
        void updatePurchaseShouldAllowEditingWithSameArchivedType() {

            // Arrange
            PurchaseType archived = type(5L, false);
            Purchase purchase = Purchase.builder()
                    .currentAccount(mock(CurrentAccount.class))
                    .purchaseType(archived)
                    .unit(1)
                    .kilograms(new BigDecimal("100.00"))
                    .build();
            PurchaseRequestDto request = PurchaseRequestDto.builder()
                    .purchaseTypeId(5L)
                    .unit(1)
                    .kilograms(new BigDecimal("100.00"))
                    .pricePerKilo(new BigDecimal("10.00"))
                    .date(LocalDate.now())
                    .build();

            when(purchaseRepository.findById(1L)).thenReturn(Optional.of(purchase));
            when(purchaseTypeRepository.findById(5L)).thenReturn(Optional.of(archived));
            when(purchaseRepository.saveAndFlush(purchase)).thenReturn(purchase);

            // Act
            service.updatePurchase(1L, request);

            // Assert
            verify(purchaseRepository).saveAndFlush(purchase);
        }

    }

}