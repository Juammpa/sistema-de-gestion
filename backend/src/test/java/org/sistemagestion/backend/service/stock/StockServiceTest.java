package org.sistemagestion.backend.service.stock;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sistemagestion.backend.dto.stock.StockCutsRequestDto;
import org.sistemagestion.backend.dto.stock.StockItemDto;
import org.sistemagestion.backend.dto.stock.StockPurchaseDto;
import org.sistemagestion.backend.dto.stock.StockResponseDto;
import org.sistemagestion.backend.exception.BusinessRuleException;
import org.sistemagestion.backend.exception.ResourceNotFoundException;
import org.sistemagestion.backend.model.*;
import org.sistemagestion.backend.model.enums.Specie;
import org.sistemagestion.backend.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StockService")
class StockServiceTest {

    @Mock private PurchaseRepository purchaseRepository;
    @Mock private PurchasePieceWeightRepository pieceWeightRepository;
    @Mock private CutMeatRepository cutMeatRepository;
    @Mock private StockCutRepository stockCutRepository;
    @Mock private StockRepository stockRepository;

    @Captor private ArgumentCaptor<List<StockCut>> rowsCaptor;

    @InjectMocks
    private StockService service;

    // ====== Helpers ======

    private Purchase purchase(int units, String kilograms, Specie specie) {
        return Purchase.builder()
                .unit(units)
                .kilograms(new BigDecimal(kilograms))
                .purchaseType(PurchaseType.builder().specie(specie).build())
                .build();
    }

    private CutMeat cutMeat(long id, String name, Specie specie, boolean active) {
        return CutMeat.builder()
                .id(id)
                .cutMeatName(name)
                .specie(specie)
                .isActive(active)
                .build();
    }

    private StockCutsRequestDto.Cut cut(long cutMeatId, String kilograms) {
        return new StockCutsRequestDto.Cut(cutMeatId, new BigDecimal(kilograms));
    }

    // Una compra de 2 unidades y 200kg (100kg cada pieza) con los cortes Asado y Rueda activos
    private Purchase givenBovinePurchaseWithCuts() {
        Purchase purchase = purchase(2, "200.00", Specie.BOVINE);
        when(purchaseRepository.findById(1L)).thenReturn(Optional.of(purchase));
        return purchase;
    }

    @Nested
    @DisplayName("updatePieceCuts()")
    class UpdatePieceCutsTests {

        @Test
        @DisplayName("Reemplaza los cortes de la pieza por los nuevos")
        void updatePieceCutsReplacesTheCutsOfThePiece() {

            // Arrange
            Purchase purchase = givenBovinePurchaseWithCuts();
            when(cutMeatRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(
                    cutMeat(1L, "Asado", Specie.BOVINE, true),
                    cutMeat(2L, "Rueda", Specie.BOVINE, true)
            ));

            // Act
            service.updatePieceCuts(1L, 1, List.of(cut(1L, "30.00"), cut(2L, "15.00")));

            // Assert: primero se borran los cortes anteriores y despues se guardan los nuevos
            InOrder order = inOrder(stockCutRepository);

            order.verify(stockCutRepository).deleteByPurchaseIdAndPieceNumber(1L, 1);
            order.verify(stockCutRepository).saveAll(rowsCaptor.capture());

            List<StockCut> rows = rowsCaptor.getValue();

            assertThat(rows).hasSize(2);
            assertThat(rows).allSatisfy(row -> {
                assertThat(row.getPurchase()).isSameAs(purchase);
                assertThat(row.getPieceNumber()).isEqualTo(1);
            });
            assertThat(rows.get(0).getKilograms()).isEqualByComparingTo("30.00");
            assertThat(rows.get(1).getKilograms()).isEqualByComparingTo("15.00");
        }

        @Test
        @DisplayName("Permite varias unidades del mismo corte en una pieza")
        void updatePieceCutsAllowsTheSameCutMoreThanOnce() {

            // Arrange
            givenBovinePurchaseWithCuts();
            when(cutMeatRepository.findAllById(List.of(1L))).thenReturn(List.of(
                    cutMeat(1L, "Asado", Specie.BOVINE, true)
            ));

            // Act: dos unidades de Asado con distinto peso
            service.updatePieceCuts(1L, 1, List.of(cut(1L, "8.00"), cut(1L, "9.50")));

            // Assert
            inOrder(stockCutRepository).verify(stockCutRepository).saveAll(rowsCaptor.capture());
            assertThat(rowsCaptor.getValue()).hasSize(2);
            assertThat(rowsCaptor.getValue().get(0).getKilograms()).isEqualByComparingTo("8.00");
            assertThat(rowsCaptor.getValue().get(1).getKilograms()).isEqualByComparingTo("9.50");
        }

        @Test
        @DisplayName("Acepta que los cortes sumen menos que la pieza (la diferencia es merma)")
        void updatePieceCutsAcceptsLessThanThePieceWeight() {

            // Arrange
            givenBovinePurchaseWithCuts();
            when(cutMeatRepository.findAllById(List.of(1L))).thenReturn(List.of(
                    cutMeat(1L, "Asado", Specie.BOVINE, true)
            ));

            // Act
            service.updatePieceCuts(1L, 1, List.of(cut(1L, "40.00")));

            // Assert
            inOrder(stockCutRepository).verify(stockCutRepository).saveAll(rowsCaptor.capture());
            assertThat(rowsCaptor.getValue()).hasSize(1);
        }

        @Test
        @DisplayName("Acepta que los cortes sumen exactamente el peso de la pieza")
        void updatePieceCutsAcceptExactlyThePieceWeight() {

            // Arrange
            givenBovinePurchaseWithCuts();
            when(cutMeatRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(
                    cutMeat(1L, "Asado", Specie.BOVINE, true),
                    cutMeat(2L, "Rueda", Specie.BOVINE, true)
            ));

            // Act: 60 + 40 = 100kg, el peso de la pieza
            service.updatePieceCuts(1L, 1, List.of(cut(1L, "60.00"), cut(2L, "40.00")));

            // Assert
            inOrder(stockCutRepository).verify(stockCutRepository).saveAll(rowsCaptor.capture());
            assertThat(rowsCaptor.getValue()).hasSize(2);
        }

        @Test
        @DisplayName("Una lista vacia deja la pieza sin cortes")
        void updatePieceCutsWithEmptyListRemovesTheCuts() {

            // Arrange
            givenBovinePurchaseWithCuts();

            // Act
            service.updatePieceCuts(1L, 1, List.of());

            // Assert
            InOrder order = inOrder(stockCutRepository);

            order.verify(stockCutRepository).deleteByPurchaseIdAndPieceNumber(1L, 1);
            order.verify(stockCutRepository).saveAll(rowsCaptor.capture());
            assertThat(rowsCaptor.getValue()).isEmpty();
        }

        @Test
        @DisplayName("Rechaza cortes que suman mas que la pieza y no toca los existentes")
        void updatePieceCutsRejectsCutsHeavierThanThePiece() {

            // Arrange
            givenBovinePurchaseWithCuts();
            when(cutMeatRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(
                    cutMeat(1L, "Asado", Specie.BOVINE, true),
                    cutMeat(2L, "Rueda", Specie.BOVINE, true)
            ));

            // Act + Assert: 60 + 50 = 110 kg y la pieza pesa 100 kg
            assertThatThrownBy(() -> service.updatePieceCuts(1L, 1, List.of(cut(1L, "60.00"), cut(2L, "50.00"))))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("110,00")
                    .hasMessageContaining("100,00");

            verifyNoInteractions(stockCutRepository);
        }

        @Test
        @DisplayName("Usa el peso ajustado de la pieza y no el reparto parejo")
        void updatePieceCutsUsesTheAdjustedPieceWeight() {

            // Arrange: piezas ajustadas a 170 y 30 kg (suman los 200 kg de la compra; el reparto parejo seria 100 y 100)
            Purchase purchase = givenBovinePurchaseWithCuts();
            when(pieceWeightRepository.findByPurchaseIdOrderByPieceNumber(1L)).thenReturn(List.of(
                    PurchasePieceWeight.builder().purchase(purchase).pieceNumber(1).kilograms(new BigDecimal("170.00")).build(),
                    PurchasePieceWeight.builder().purchase(purchase).pieceNumber(2).kilograms(new BigDecimal("30.00")).build()));
            when(cutMeatRepository.findAllById(List.of(1L))).thenReturn(List.of(
                    cutMeat(1L, "Asado", Specie.BOVINE, true)));

            // Act + Assert: 40 kg entran en una pieza de 100 pero no en la pieza 2 de 30
            assertThatThrownBy(() -> service.updatePieceCuts(1L, 2, List.of(cut(1L, "40.00"))))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("30,00");
        }

        @Test
        @DisplayName("Rechaza una pieza que no existe en la compra")
        void updatePieceCutsRejectsAPieceThatDoesNotExist() {

            // Arrange: la compra tiene 2 piezas
            givenBovinePurchaseWithCuts();

            // Act + Assert
            assertThatThrownBy(() -> service.updatePieceCuts(1L, 3, List.of()))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("la pieza 3 no existe");
            assertThatThrownBy(() -> service.updatePieceCuts(1L, 0, List.of()))
                    .isInstanceOf(BusinessRuleException.class);

            verifyNoInteractions(stockCutRepository);
        }

        @Test
        @DisplayName("Rechaza una compra que no existe")
        void updatePieceCutsRejectsAPurchaseThatDoesNotExist() {

            // Arrange
            when(purchaseRepository.findById(99L)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> service.updatePieceCuts(99L, 1, List.of()))
                    .isInstanceOf(ResourceNotFoundException.class);

            verifyNoInteractions(stockCutRepository);
        }

        @Test
        @DisplayName("Rechaza un corte que no existe (o es de otro usuario)")
        void updatePieceCutsRejectsACutThatDoesNotExist() {

            // Arrange: el repositorio no devuelve el corte
            givenBovinePurchaseWithCuts();
            when(cutMeatRepository.findAllById(List.of(7L))).thenReturn(List.of());

            // Act + Assert
            assertThatThrownBy(() -> service.updatePieceCuts(1L, 1, List.of(cut(7L, "10.00"))))
                    .isInstanceOf(ResourceNotFoundException.class);

            verifyNoInteractions(stockCutRepository);
        }

        @Test
        @DisplayName("Rechaza un corte eliminado")
        void updatePieceCutsRejectsAnInactiveCut() {

            // Arrange
            givenBovinePurchaseWithCuts();
            when(cutMeatRepository.findAllById(List.of(1L))).thenReturn(List.of(
                    cutMeat(1L, "Asado", Specie.BOVINE, false)));

            // Act + Assert
            assertThatThrownBy(() -> service.updatePieceCuts(1L, 1, List.of(cut(1L, "10.00"))))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("no está disponible");

            verifyNoInteractions(stockCutRepository);
        }

        @Test
        @DisplayName("Rechaza un corte de otra especie")
        void updatePieceCutsRejectsACutOfAnotherSpecie() {

            // Arrange: compra bovina y corte porcino
            givenBovinePurchaseWithCuts();
            when(cutMeatRepository.findAllById(List.of(1L))).thenReturn(List.of(
                    cutMeat(1L, "Bondiola", Specie.PORCINE, true)));

            // Act + Assert
            assertThatThrownBy(() -> service.updatePieceCuts(1L, 1, List.of(cut(1L, "10.00"))))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("no corresponde a la especie");

            verifyNoInteractions(stockCutRepository);
        }
    }

    @Nested
    @DisplayName("getStock()")
    class GetStockTests {

        @Test
        @DisplayName("Cada pieza trae sus propios cortes y las piezas sin cortes, una lista vacia")
        void getStockAttachesTheCutsToTheirPiece() {

            // Arrange: una compra de 2 piezas, con dos cortes en la pieza 1 y ninguno en la 2
            LocalDate day = LocalDate.of(2026, 10, 26);
            Purchase purchase = Purchase.builder().id(1L).build();
            CutMeat asado = cutMeat(1L, "Asado", Specie.BOVINE, true);

            when(stockRepository.findPurchases(day, day)).thenReturn(List.of(new StockPurchaseDto(
                    1L, day, 10L, "Ternero hasta 110kg", Specie.BOVINE, 2, new BigDecimal("200.00"), 5L, "Friorifico Sur"
            )));
            when(stockCutRepository.findByPurchaseIdIn(List.of(1L))).thenReturn(List.of(
                    StockCut.builder().id(100L).purchase(purchase).pieceNumber(1).cutMeat(asado).kilograms(new BigDecimal("8.00")).build(),
                    StockCut.builder().id(101L).purchase(purchase).pieceNumber(1).cutMeat(asado).kilograms(new BigDecimal("9.50")).build()
            ));

            // Act
            StockResponseDto response = service.getStock(day, day);

            // Assert
            assertThat(response.items()).hasSize(2);
            assertThat(response.items().get(0).cuts())
                    .extracting(StockItemDto.Cut::id, StockItemDto.Cut::cutMeatName)
                    .containsExactly(tuple(100L, "Asado"), tuple(101L, "Asado"));
            assertThat(response.items().get(1).cuts()).isEmpty();

        }

    }

    @Nested
    @DisplayName("updatePieceWeights()")
    class UpdatePieceWeightsTests {

        // Una pieza 2 con 40 kg de cortes
        private void givenCutsOnPieceTwo(Purchase purchase) {
            when(stockCutRepository.findByPurchaseId(1L)).thenReturn(List.of(
                    StockCut.builder().purchase(purchase).pieceNumber(2)
                            .cutMeat(cutMeat(1L, "Asado", Specie.BOVINE, true))
                            .kilograms(new BigDecimal("40.00")).build()));
        }

        @Test
        @DisplayName("Rechaza achicar una pieza por debajo de sus cortes y no cambia el reparto")
        void updatePieceWeightsRejectsAWeightBelowTheCuts() {

            // Arrange
            Purchase purchase = givenBovinePurchaseWithCuts();
            givenCutsOnPieceTwo(purchase);

            // Act + Assert: la pieza 2 pasa a pesar 30 kg y tiene 40 kg de cortes
            assertThatThrownBy(() -> service.updatePieceWeights(1L,
                    List.of(new BigDecimal("170.00"), new BigDecimal("30.00"))))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("La pieza 2")
                    .hasMessageContaining("40,00")
                    .hasMessageContaining("30,00");

            verifyNoInteractions(pieceWeightRepository);
        }

        @Test
        @DisplayName("Permite cambiar el reparto si los cortes siguen entrando")
        void updatePieceWeightsAcceptsAWeightThatFitsTheCuts() {

            // Arrange
            Purchase purchase = givenBovinePurchaseWithCuts();
            givenCutsOnPieceTwo(purchase);

            // Act: la pieza 2 pasa a pesar 50 kg y tiene 40 kg de cortes
            service.updatePieceWeights(1L, List.of(new BigDecimal("150.00"), new BigDecimal("50.00")));

            // Assert
            verify(pieceWeightRepository).deleteByPurchaseId(1L);
        }
    }


}
