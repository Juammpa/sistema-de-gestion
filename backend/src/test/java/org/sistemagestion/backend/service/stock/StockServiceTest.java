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
    @Mock private StockPieceClosureRepository closureRepository;

    @Captor private ArgumentCaptor<List<StockCut>> rowsCaptor;
    @Captor private ArgumentCaptor<StockPieceClosure> closureCaptor;

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
        return new StockCutsRequestDto.Cut(null, cutMeatId, new BigDecimal(kilograms));
    }

    // Una unidad que ya existe en la pieza: viaja con su id
    private StockCutsRequestDto.Cut existingCut(long id, long cutMeatId, String kilograms) {
        return new StockCutsRequestDto.Cut(id, cutMeatId, new BigDecimal(kilograms));
    }

    private StockCut stockCut(long id, Purchase purchase, int pieceNumber, CutMeat cutMeat, String kilograms, Sale sale) {
        return StockCut.builder().id(id).purchase(purchase).pieceNumber(pieceNumber).cutMeat(cutMeat).kilograms(new BigDecimal(kilograms)).sale(sale).build();
    }

    // Una unidad de Asado de 30 kg en la pieza 1, ya vendida
    private StockCut soldUnit(Purchase purchase) {
        return stockCut(100L, purchase, 1, cutMeat(1L, "Asado", Specie.BOVINE, true), "30.00", Sale.builder().id(50L).build());
    }

    // Con las reglas fallidas no se debe escribir nada: ni borrar ni guardar
    private void verifyNothingWasWritten() {
        verify(stockCutRepository, never()).deleteAll(anyList());
        verify(stockCutRepository, never()).saveAll(anyList());
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
        @DisplayName("Guarda los cortes nuevos de la pieza")
        void updatePieceCutsSavesNewCuts() {

            // Arrange
            Purchase purchase = givenBovinePurchaseWithCuts();
            when(cutMeatRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(
                    cutMeat(1L, "Asado", Specie.BOVINE, true),
                    cutMeat(2L, "Rueda", Specie.BOVINE, true)));

            // Act
            service.updatePieceCuts(1L, 1, List.of(cut(1L, "30.00"), cut(2L, "15.00")));

            // Assert
            verify(stockCutRepository).deleteAll(List.of());
            verify(stockCutRepository).saveAll(rowsCaptor.capture());

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
        @DisplayName("Aplica altas, cambios y bajas de las unidades de la pieza")
        void updatePieceCutsAppliesAdditionsChangesAndRemovals() {

            // Arrange: la pieza tiene Asado 30 (id 100) y Rueda 15 (id 101)
            Purchase purchase = givenBovinePurchaseWithCuts();
            CutMeat asado = cutMeat(1L, "Asado", Specie.BOVINE, true);
            StockCut asadoUnit = stockCut(100L, purchase, 1, asado, "30.00", null);
            StockCut ruedaUnit = stockCut(101L, purchase, 1, cutMeat(2L, "Rueda", Specie.BOVINE, true), "15.00", null);
            when(stockCutRepository.findByPurchaseIdAndPieceNumber(1L, 1)).thenReturn(List.of(asadoUnit, ruedaUnit));
            when(cutMeatRepository.findAllById(List.of(1L))).thenReturn(List.of(asado));

            // Act: el Asado pasa a 32 kg, se agrega otro Asado de 8 kg y se quita la Rueda
            service.updatePieceCuts(1L, 1, List.of(existingCut(100L, 1L, "32.00"), cut(1L, "8.00")));

            // Assert
            verify(stockCutRepository).deleteAll(List.of(ruedaUnit));
            verify(stockCutRepository).saveAll(rowsCaptor.capture());

            List<StockCut> rows = rowsCaptor.getValue();
            assertThat(rows).hasSize(2);
            assertThat(rows.get(0)).isSameAs(asadoUnit);
            assertThat(rows.get(0).getKilograms()).isEqualByComparingTo("32.00");
            assertThat(rows.get(1).getId()).isNull();
            assertThat(rows.get(1).getKilograms()).isEqualByComparingTo("8.00");
        }

        @Test
        @DisplayName("Permite varias unidades del mismo corte en una pieza")
        void updatePieceCutsAllowsTheSameCutMoreThanOnce() {

            // Arrange
            givenBovinePurchaseWithCuts();
            when(cutMeatRepository.findAllById(List.of(1L))).thenReturn(List.of(
                    cutMeat(1L, "Asado", Specie.BOVINE, true)));

            // Act: dos unidades de Asado con distinto peso
            service.updatePieceCuts(1L, 1, List.of(cut(1L, "8.00"), cut(1L, "9.50")));

            // Assert
            verify(stockCutRepository).saveAll(rowsCaptor.capture());
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
                    cutMeat(1L, "Asado", Specie.BOVINE, true)));

            // Act
            service.updatePieceCuts(1L, 1, List.of(cut(1L, "40.00")));

            // Assert
            verify(stockCutRepository).saveAll(rowsCaptor.capture());
            assertThat(rowsCaptor.getValue()).hasSize(1);
        }

        @Test
        @DisplayName("Acepta que los cortes sumen exactamente el peso de la pieza")
        void updatePieceCutsAcceptsExactlyThePieceWeight() {

            // Arrange
            givenBovinePurchaseWithCuts();
            when(cutMeatRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(
                    cutMeat(1L, "Asado", Specie.BOVINE, true),
                    cutMeat(2L, "Rueda", Specie.BOVINE, true)));

            // Act: 60 + 40 = 100 kg, el peso de la pieza
            service.updatePieceCuts(1L, 1, List.of(cut(1L, "60.00"), cut(2L, "40.00")));

            // Assert
            verify(stockCutRepository).saveAll(rowsCaptor.capture());
            assertThat(rowsCaptor.getValue()).hasSize(2);
        }

        @Test
        @DisplayName("Una lista vacía quita las unidades que no están vendidas")
        void updatePieceCutsWithEmptyListRemovesTheUnsoldCuts() {

            // Arrange
            Purchase purchase = givenBovinePurchaseWithCuts();
            StockCut unit = stockCut(100L, purchase, 1, cutMeat(1L, "Asado", Specie.BOVINE, true), "30.00", null);
            when(stockCutRepository.findByPurchaseIdAndPieceNumber(1L, 1)).thenReturn(List.of(unit));

            // Act
            service.updatePieceCuts(1L, 1, List.of());

            // Assert
            verify(stockCutRepository).deleteAll(List.of(unit));
            verify(stockCutRepository).saveAll(rowsCaptor.capture());
            assertThat(rowsCaptor.getValue()).isEmpty();
        }

        @Test
        @DisplayName("Rechaza cortes que suman más que la pieza y no escribe nada")
        void updatePieceCutsRejectsCutsHeavierThanThePiece() {

            // Arrange
            givenBovinePurchaseWithCuts();
            when(cutMeatRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(
                    cutMeat(1L, "Asado", Specie.BOVINE, true),
                    cutMeat(2L, "Rueda", Specie.BOVINE, true)));

            // Act + Assert: 60 + 50 = 110 kg y la pieza pesa 100 kg
            assertThatThrownBy(() -> service.updatePieceCuts(1L, 1, List.of(cut(1L, "60.00"), cut(2L, "50.00"))))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("110,00")
                    .hasMessageContaining("100,00");

            verifyNothingWasWritten();
        }

        @Test
        @DisplayName("Usa el peso ajustado de la pieza y no el reparto parejo")
        void updatePieceCutsUsesTheAdjustedPieceWeight() {

            // Arrange: piezas ajustadas a 170 y 30 kg (suman los 200 kg de la compra; el reparto parejo sería 100 y 100)
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

            verifyNothingWasWritten();
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

            verifyNothingWasWritten();
        }

        @Test
        @DisplayName("Rechaza una compra que no existe")
        void updatePieceCutsRejectsAPurchaseThatDoesNotExist() {

            // Arrange
            when(purchaseRepository.findById(99L)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> service.updatePieceCuts(99L, 1, List.of()))
                    .isInstanceOf(ResourceNotFoundException.class);

            verifyNothingWasWritten();
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

            verifyNothingWasWritten();
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

            verifyNothingWasWritten();
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

            verifyNothingWasWritten();
        }

        @Test
        @DisplayName("Conserva las unidades vendidas sin tocarlas y guarda solo las demás")
        void updatePieceCutsKeepsTheSoldUnitsUntouched() {

            // Arrange: la pieza tiene un Asado de 30 kg vendido
            Purchase purchase = givenBovinePurchaseWithCuts();
            StockCut sold = soldUnit(purchase);
            when(stockCutRepository.findByPurchaseIdAndPieceNumber(1L, 1)).thenReturn(List.of(sold));
            when(cutMeatRepository.findAllById(List.of(2L))).thenReturn(List.of(
                    cutMeat(2L, "Rueda", Specie.BOVINE, true)));

            // Act: el pedido trae la unidad vendida idéntica y una Rueda nueva
            service.updatePieceCuts(1L, 1, List.of(existingCut(100L, 1L, "30.00"), cut(2L, "15.00")));

            // Assert: solo se guarda la Rueda; la unidad vendida no se borra ni se vuelve a guardar
            verify(stockCutRepository).deleteAll(List.of());
            verify(stockCutRepository).saveAll(rowsCaptor.capture());
            assertThat(rowsCaptor.getValue()).hasSize(1);
            assertThat(rowsCaptor.getValue().get(0).getKilograms()).isEqualByComparingTo("15.00");
            assertThat(sold.isSold()).isTrue();
        }

        @Test
        @DisplayName("Rechaza quitar una unidad vendida")
        void updatePieceCutsRejectsRemovingASoldUnit() {

            // Arrange
            Purchase purchase = givenBovinePurchaseWithCuts();
            when(stockCutRepository.findByPurchaseIdAndPieceNumber(1L, 1)).thenReturn(List.of(soldUnit(purchase)));

            // Act + Assert
            assertThatThrownBy(() -> service.updatePieceCuts(1L, 1, List.of()))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("ya fue vendida");

            verifyNothingWasWritten();
        }

        @Test
        @DisplayName("Rechaza cambiar el peso de una unidad vendida")
        void updatePieceCutsRejectsChangingTheWeightOfASoldUnit() {

            // Arrange
            Purchase purchase = givenBovinePurchaseWithCuts();
            when(stockCutRepository.findByPurchaseIdAndPieceNumber(1L, 1)).thenReturn(List.of(soldUnit(purchase)));

            // Act + Assert: la unidad vendida pesaba 30 kg
            assertThatThrownBy(() -> service.updatePieceCuts(1L, 1, List.of(existingCut(100L, 1L, "25.00"))))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("ya fue vendida");

            verifyNothingWasWritten();
        }

        @Test
        @DisplayName("Rechaza cambiar el corte de una unidad vendida")
        void updatePieceCutsRejectsChangingTheCutOfASoldUnit() {

            // Arrange
            Purchase purchase = givenBovinePurchaseWithCuts();
            when(stockCutRepository.findByPurchaseIdAndPieceNumber(1L, 1)).thenReturn(List.of(soldUnit(purchase)));

            // Act + Assert: la unidad vendida es de Asado (id 1) y se intenta pasar a Rueda (id 2)
            assertThatThrownBy(() -> service.updatePieceCuts(1L, 1, List.of(existingCut(100L, 2L, "30.00"))))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("ya fue vendida");

            verifyNothingWasWritten();
        }

        @Test
        @DisplayName("Las unidades vendidas cuentan para el peso máximo de la pieza")
        void updatePieceCutsCountsTheSoldUnitsTowardTheMaximum() {

            // Arrange: ya hay 30 kg vendidos y se intenta agregar 80 kg más en una pieza de 100
            Purchase purchase = givenBovinePurchaseWithCuts();
            when(stockCutRepository.findByPurchaseIdAndPieceNumber(1L, 1)).thenReturn(List.of(soldUnit(purchase)));
            when(cutMeatRepository.findAllById(List.of(2L))).thenReturn(List.of(
                    cutMeat(2L, "Rueda", Specie.BOVINE, true)));

            // Act + Assert
            assertThatThrownBy(() -> service.updatePieceCuts(1L, 1, List.of(existingCut(100L, 1L, "30.00"), cut(2L, "80.00"))))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("110,00");

            verifyNothingWasWritten();
        }

        @Test
        @DisplayName("Rechaza un id que no es de una unidad de esta pieza")
        void updatePieceCutsRejectsAnIdThatDoesNotBelongToThePiece() {

            // Arrange
            givenBovinePurchaseWithCuts();

            // Act + Assert
            assertThatThrownBy(() -> service.updatePieceCuts(1L, 1, List.of(existingCut(999L, 1L, "10.00"))))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("no pertenece a esta pieza");

            verifyNothingWasWritten();
        }

        @Test
        @DisplayName("Rechaza repetir el id de una misma unidad")
        void updatePieceCutsRejectsARepeatedId() {

            // Arrange
            Purchase purchase = givenBovinePurchaseWithCuts();
            StockCut unit = stockCut(100L, purchase, 1, cutMeat(1L, "Asado", Specie.BOVINE, true), "30.00", null);
            when(stockCutRepository.findByPurchaseIdAndPieceNumber(1L, 1)).thenReturn(List.of(unit));

            // Act + Assert
            assertThatThrownBy(() -> service.updatePieceCuts(1L, 1,
                    List.of(existingCut(100L, 1L, "10.00"), existingCut(100L, 1L, "10.00"))))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("no pertenece a esta pieza");

            verifyNothingWasWritten();
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

    @Nested
    @DisplayName("closePiece()")
    class ClosePieceTests {

        @Test
        @DisplayName("Cierra la pieza cuando todos sus cortes estan vendidos")
        void closePieceSavesTheClosureWhenAllCutsAreSold() {

            // Arrange
            Purchase purchase = givenBovinePurchaseWithCuts();
            when(stockCutRepository.findByPurchaseIdAndPieceNumber(1L, 1))
                    .thenReturn(List.of(soldUnit(purchase)));

            // Act
            service.closePiece(1L, 1);

            // Assert
            verify(closureRepository).save(closureCaptor.capture());
            assertThat(closureCaptor.getValue().getPurchase()).isSameAs(purchase);
            assertThat(closureCaptor.getValue().getPieceNumber()).isEqualTo(1);
        }

        @Test
        @DisplayName("Rechaza cerrar una pieza que todavia tiene un corte sin vender")
        void closePieceRejectsWhenACutIsUnsold() {

            // Arrange: la pieza tiene un corte vendido y otro disponible
            Purchase purchase = givenBovinePurchaseWithCuts();
            when(stockCutRepository.findByPurchaseIdAndPieceNumber(1L, 1)).thenReturn(List.of(
                    soldUnit(purchase),
                    stockCut(101L, purchase, 1, cutMeat(2L, "Rueda", Specie.BOVINE, true), "15.00", null)));

            // Act + Assert
            assertThatThrownBy(() -> service.closePiece(1L, 1))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("todos sus cortes ya fueron vendidos");

            verify(closureRepository, never()).save(any());
        }

        @Test
        @DisplayName("Rechaza cerrar una pieza sin cortes")
        void closePieceRejectsWhenThePieceHasNoCuts() {

            // Arrange
            givenBovinePurchaseWithCuts();

            // Act + Assert
            assertThatThrownBy(() -> service.closePiece(1L, 1))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("todos sus cortes ya fueron vendidos");

            verify(closureRepository, never()).save(any());
        }

        @Test
        @DisplayName("No vuelve a guardar el cierre si la pieza ya estaba cerrada")
        void closePieceDoesNothingWhenAlreadyClosed() {

            // Arrange
            Purchase purchase = givenBovinePurchaseWithCuts();
            when(stockCutRepository.findByPurchaseIdAndPieceNumber(1L, 1))
                    .thenReturn(List.of(soldUnit(purchase)));
            when(closureRepository.existsByPurchaseIdAndPieceNumber(1L, 1)).thenReturn(true);

            // Act
            service.closePiece(1L, 1);

            // Assert
            verify(closureRepository, never()).save(any());
        }

        @Test
        @DisplayName("Rechaza una pieza que no existe en la compra")
        void closePieceRejectsAPieceThatDoesNotExist() {

            // Arrange: la compra tiene 2 piezas
            givenBovinePurchaseWithCuts();

            // Act + Assert
            assertThatThrownBy(() -> service.closePiece(1L, 3))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("la pieza 3 no existe");

            verifyNoInteractions(stockCutRepository, closureRepository);
        }

        @Test
        @DisplayName("Falla si la compra no existe")
        void closePieceFailsWhenThePurchaseDoesNotExist() {

            // Arrange
            when(purchaseRepository.findById(9L)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> service.closePiece(9L, 1))
                    .isInstanceOf(ResourceNotFoundException.class);

            verifyNoInteractions(closureRepository);
        }
    }

    @Nested
    @DisplayName("reopenPiece()")
    class ReopenPieceTests {

        @Test
        @DisplayName("Borra el cierre de la pieza")
        void reopenPieceDeletesTheClosure() {

            // Arrange
            givenBovinePurchaseWithCuts();

            // Act
            service.reopenPiece(1L, 1);

            // Assert
            verify(closureRepository).deleteByPurchaseIdAndPieceNumber(1L, 1);
        }

        @Test
        @DisplayName("Rechaza una pieza que no existe en la compra")
        void reopenPieceRejectsAPieceThatDoesNotExist() {

            // Arrange
            givenBovinePurchaseWithCuts();

            // Act + Assert
            assertThatThrownBy(() -> service.reopenPiece(1L, 3))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("la pieza 3 no existe");

            verifyNoInteractions(closureRepository);
        }
    }

    @Nested
    @DisplayName("updatePieceCuts() con la pieza cerrada")
    class UpdateClosedPieceCutsTests {

        @Test
        @DisplayName("Rechaza cambiar los cortes de una pieza cerrada")
        void updatePieceCutsRejectsAClosedPiece() {

            // Arrange: la pieza tiene un unico corte, vendido, y esta cerrada
            Purchase purchase = givenBovinePurchaseWithCuts();
            when(stockCutRepository.findByPurchaseIdAndPieceNumber(1L, 1))
                    .thenReturn(List.of(soldUnit(purchase)));
            when(closureRepository.existsByPurchaseIdAndPieceNumber(1L, 1)).thenReturn(true);

            // Act + Assert: intenta agregar un corte nuevo (entraria en el peso: hay merma)
            assertThatThrownBy(() -> service.updatePieceCuts(1L, 1,
                    List.of(existingCut(100L, 1L, "30.00"), cut(1L, "10.00"))))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("cerrada como vendida");

            verifyNothingWasWritten();
            verify(closureRepository, never()).deleteByPurchaseIdAndPieceNumber(any(), any());
        }

        @Test
        @DisplayName("Si el cierre quedo viejo (un corte volvio a estar disponible), lo borra y permite editar")
        void updatePieceCutsDropsAStaleClosure() {

            // Arrange: hay un cierre guardado, pero el corte ya no esta vendido
            Purchase purchase = givenBovinePurchaseWithCuts();
            CutMeat asado = cutMeat(1L, "Asado", Specie.BOVINE, true);
            when(stockCutRepository.findByPurchaseIdAndPieceNumber(1L, 1))
                    .thenReturn(List.of(stockCut(100L, purchase, 1, asado, "30.00", null)));
            when(closureRepository.existsByPurchaseIdAndPieceNumber(1L, 1)).thenReturn(true);
            when(cutMeatRepository.findAllById(List.of(1L))).thenReturn(List.of(asado));

            // Act
            service.updatePieceCuts(1L, 1, List.of(existingCut(100L, 1L, "30.00")));

            // Assert
            verify(closureRepository).deleteByPurchaseIdAndPieceNumber(1L, 1);
            verify(stockCutRepository).saveAll(anyList());
        }
    }

    @Nested
    @DisplayName("getStock() con cierres de pieza")
    class GetStockClosuresTests {

        private final LocalDate day = LocalDate.of(2026, 10, 30);
        private final Purchase purchase = Purchase.builder().id(1L).build();

        // Una compra de una sola pieza de 100 kg, con los cortes indicados
        private void givenOnePiece(StockCut... cuts) {
            when(stockRepository.findPurchases(day, day)).thenReturn(List.of(new StockPurchaseDto(
                    1L, day, 10L, "Media Cerdo", Specie.PORCINE, 1, new BigDecimal("100.00"), 5L, "La Estancia")));

            if (cuts.length > 0) {
                when(stockCutRepository.findByPurchaseIdIn(List.of(1L))).thenReturn(List.of(cuts));
            }
        }

        private void givenPieceIsClosed() {
            when(closureRepository.findByPurchaseIdIn(List.of(1L))).thenReturn(List.of(
                    StockPieceClosure.builder().purchase(purchase).pieceNumber(1).build()));
        }

        // 90 kg vendidos de una pieza de 100: los 10 restantes son merma
        private StockCut soldCut() {
            return stockCut(100L, purchase, 1, cutMeat(1L, "Media Cerdo", Specie.PORCINE, true),
                    "90.00", Sale.builder().id(50L).build());
        }

        private StockCut unsoldCut() {
            return stockCut(101L, purchase, 1, cutMeat(1L, "Media Cerdo", Specie.PORCINE, true),
                    "10.00", null);
        }

        @Test
        @DisplayName("Una pieza cerrada con todos sus cortes vendidos figura como vendida, aunque haya merma")
        void getStockMarksAClosedPieceAsSoldOut() {

            // Arrange
            givenOnePiece(soldCut());
            givenPieceIsClosed();

            // Act
            StockItemDto item = service.getStock(day, day).items().get(0);

            // Assert
            assertThat(item.soldOut()).isTrue();
            assertThat(item.canClose()).isFalse();
        }

        @Test
        @DisplayName("Una pieza abierta con todos sus cortes vendidos se puede cerrar")
        void getStockOffersToCloseAFullySoldPiece() {

            // Arrange
            givenOnePiece(soldCut());

            // Act
            StockItemDto item = service.getStock(day, day).items().get(0);

            // Assert
            assertThat(item.soldOut()).isFalse();
            assertThat(item.canClose()).isTrue();
        }

        @Test
        @DisplayName("Un cierre viejo no cuenta si volvio a haber un corte sin vender")
        void getStockIgnoresAStaleClosure() {

            // Arrange
            givenOnePiece(soldCut(), unsoldCut());
            givenPieceIsClosed();

            // Act
            StockItemDto item = service.getStock(day, day).items().get(0);

            // Assert
            assertThat(item.soldOut()).isFalse();
            assertThat(item.canClose()).isFalse();
        }

        @Test
        @DisplayName("Una pieza sin cortes no figura como vendida ni se puede cerrar")
        void getStockDoesNotCloseAPieceWithoutCuts() {

            // Arrange
            givenOnePiece();

            // Act
            StockItemDto item = service.getStock(day, day).items().get(0);

            // Assert
            assertThat(item.soldOut()).isFalse();
            assertThat(item.canClose()).isFalse();
        }
    }


}
