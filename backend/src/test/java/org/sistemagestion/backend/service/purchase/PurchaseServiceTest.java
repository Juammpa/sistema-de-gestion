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
    @Mock private StockCutRepository stockCutRepository;
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

    private Purchase existingPurchase(PurchaseType type) {
        return Purchase.builder()
                .id(1L)
                .currentAccount(mock(CurrentAccount.class))
                .purchaseType(type)
                .unit(1)
                .kilograms(new BigDecimal("100.00"))
                .build();
    }

    private PurchaseRequestDto updateRequest(Long typeId, String kilograms, String pricePerKilo) {
        return PurchaseRequestDto.builder()
                .purchaseTypeId(typeId)
                .unit(1)
                .kilograms(new BigDecimal(kilograms))
                .pricePerKilo(new BigDecimal(pricePerKilo))
                .date(LocalDate.now())
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

        @Test
        @DisplayName("Debe rechazar cambiar los kilos de una compra que ya tiene cortes")
        void updatePurchaseShouldRejectChangingKilogramsWhenItHasCuts() {

            // Arrange
            PurchaseType bovine = type(5L, true);
            Purchase purchase = existingPurchase(bovine);

            when(purchaseRepository.findById(1L)).thenReturn(Optional.of(purchase));
            when(purchaseTypeRepository.findById(5L)).thenReturn(Optional.of(bovine));
            when(stockCutRepository.existsByPurchaseId(1L)).thenReturn(true);

            // Act + Assert
            assertThatThrownBy(() -> service.updatePurchase(1L, updateRequest(5L, "120.00", "10.00")))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("tiene cortes");

            verify(purchaseRepository, never()).saveAndFlush(any());
            verifyNoInteractions(pieceWeightRepository);
        }

        @Test
        @DisplayName("Debe rechazar cambiar a otra especie una compra que ya tiene cortes")
        void updatePurchaseShouldRejectChangingTheSpecieWhenItHasCuts() {

            // Arrange: la compra es bovina y el tipo nuevo es porcino
            PurchaseType bovine = type(5L, true);
            PurchaseType porcine = PurchaseType.builder()
                    .id(6L).name("Cerdo").specie(Specie.PORCINE).isActive(true).build();
            Purchase purchase = existingPurchase(bovine);

            when(purchaseRepository.findById(1L)).thenReturn(Optional.of(purchase));
            when(purchaseTypeRepository.findById(6L)).thenReturn(Optional.of(porcine));
            when(stockCutRepository.existsByPurchaseId(1L)).thenReturn(true);

            // Act + Assert
            assertThatThrownBy(() -> service.updatePurchase(1L, updateRequest(6L, "100.00", "10.00")))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("tiene cortes");

            verify(purchaseRepository, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("Debe permitir cambiar el precio de una compra con cortes")
        void updatePurchaseShouldAllowChangingThePriceWhenItHasCuts() {

            // Arrange: mismos kilos, unidades y especie; solo cambia el precio
            PurchaseType bovine = type(5L, true);
            Purchase purchase = existingPurchase(bovine);

            when(purchaseRepository.findById(1L)).thenReturn(Optional.of(purchase));
            when(purchaseTypeRepository.findById(5L)).thenReturn(Optional.of(bovine));
            when(purchaseRepository.saveAndFlush(purchase)).thenReturn(purchase);
            // Tiene cortes, pero la regla no debe consultarse porque nada de lo que los afecta cambia
            lenient().when(stockCutRepository.existsByPurchaseId(1L)).thenReturn(true);

            // Act
            service.updatePurchase(1L, updateRequest(5L, "100.00", "25.00"));

            // Assert
            verify(purchaseRepository).saveAndFlush(purchase);
        }


    }

    @Nested
    @DisplayName("deletePurchaseById()")
    class DeletePurchaseTests {

        @Test
        @DisplayName("Debe rechazar eliminar una compra que tiene cortes")
        void deletePurchaseShouldRejectWhenItHasCuts() {

            // Arrange
            Purchase purchase = existingPurchase(type(5L, true));

            when(purchaseRepository.findById(1L)).thenReturn(Optional.of(purchase));
            when(stockCutRepository.existsByPurchaseId(1L)).thenReturn(true);

            // Act + Assert
            assertThatThrownBy(() -> service.deletePurchaseById(1L))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("tiene cortes");

            verify(purchaseRepository, never()).delete(any());
            verifyNoInteractions(pieceWeightRepository);
        }

        @Test
        @DisplayName("Debe eliminar la compra y sus pesos ajustados cuando no tiene cortes")
        void deletePurchaseShouldDeleteWhenItHasNoCuts() {

            // Arrange
            Purchase purchase = existingPurchase(type(5L, true));

            when(purchaseRepository.findById(1L)).thenReturn(Optional.of(purchase));

            // Act
            service.deletePurchaseById(1L);

            // Assert
            verify(pieceWeightRepository).deleteByPurchaseId(1L);
            verify(purchaseRepository).delete(purchase);
            verify(balanceRecalculationService).recalculate(purchase.getCurrentAccount());
        }
    }

}