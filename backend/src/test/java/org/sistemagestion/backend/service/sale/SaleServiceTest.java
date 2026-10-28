package org.sistemagestion.backend.service.sale;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sistemagestion.backend.dto.sales.SaleRequestDto;
import org.sistemagestion.backend.dto.sales.SaleResponseDto;
import org.sistemagestion.backend.dto.stock.AvailableCutDto;
import org.sistemagestion.backend.exception.BusinessRuleException;
import org.sistemagestion.backend.exception.ResourceNotFoundException;
import org.sistemagestion.backend.mapper.SaleMapper;
import org.sistemagestion.backend.model.*;
import org.sistemagestion.backend.model.enums.Specie;
import org.sistemagestion.backend.repository.*;
import org.sistemagestion.backend.service.account.AccountGuard;
import org.sistemagestion.backend.service.movement.BalanceRecalculationService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SaleService")
class SaleServiceTest {

    @Mock private SaleRepository saleRepository;
    @Mock private ClientRepository clientRepository;
    @Mock private CutMeatRepository cutMeatRepository;
    @Mock private ButcherShopRepository butcherShopRepository;
    @Mock private StockCutRepository stockCutRepository;
    @Mock private BalanceRecalculationService balanceRecalculationService;
    @Mock private AccountGuard accountGuard;

    // El mapper real: calcula importes y saldos
    @Spy private SaleMapper mapper = new SaleMapper();

    @Captor private ArgumentCaptor<Sale> saleCaptor;

    @InjectMocks
    private SaleService service;

    // ====== Helpers ======

    // balance == null simula una cuenta que todavia no tuvo saldo
    private CurrentAccount account(String balance) {
        CurrentAccount account = new CurrentAccount();
        account.setId(7L);
        account.setCurrentBalance(balance == null ? null : new BigDecimal(balance));
        return account;
    }

    private CutMeat cutMeat(long id, String name, String price, boolean active) {
        return CutMeat.builder()
                .id(id)
                .cutMeatName(name)
                .pricePerKilo(new BigDecimal(price))
                .specie(Specie.BOVINE)
                .isActive(active)
                .build();
    }

    private StockCut unit(long id, CutMeat cutMeat, String kilograms, Sale sale) {
        return StockCut.builder()
                .id(id)
                .cutMeat(cutMeat)
                .kilograms(new BigDecimal(kilograms))
                .sale(sale)
                .build();
    }

    private SaleRequestDto request(long cutMeatId, List<Long> stockCutIds) {
        return new SaleRequestDto(1L, 10L, cutMeatId, stockCutIds, LocalDate.of(2026,10,27));
    }

    // Cliente, corte y sucursal que se consultan al crear una venta
    private void givenSaleContext(CutMeat cutMeat, CurrentAccount account) {
        Client client = Client.builder().id(1L).build();

        when(clientRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(client));
        when(cutMeatRepository.findById(cutMeat.getId())).thenReturn(Optional.of(cutMeat));
        when(butcherShopRepository.findByIdAndIsActiveTrue(10L)).thenReturn(Optional.of(
                ButcherShop.builder().id(10L).client(client).currentAccount(account).build()
        ));
    }

    // Una venta ya guardada (venta 5), de 1 unidad de 8 kg
    private Sale existingSale(CutMeat cutMeat, CurrentAccount account) {
        return Sale.builder()
                .id(5L)
                .client(Client.builder().id(1L).build())
                .cutMeat(cutMeat)
                .cutMeatName(cutMeat.getCutMeatName())
                .pricePerKilo(cutMeat.getPricePerKilo())
                .currentAccount(account)
                .unit(1)
                .kilograms(new BigDecimal("8.000"))
                .amount(new BigDecimal("800.00"))
                .date(LocalDate.of(2026,10,20))
                .build();
    }


    @Nested
    @DisplayName("createSale()")
    class CreateSaleTests {

        @Test
        @DisplayName("Reserva las unidades y calcula kilos, unidades, importe y saldo")
        void createSaleReservesTheUnitsAndComputesTheTotals() {

            // Arrange: dos unidades libres de Asado (8 y 9,5 kg) a $100 el kilo, y una cuenta con $1.000
            CutMeat asado = cutMeat(1L, "Asado", "100.00", true);
            CurrentAccount account = account("1000.00");
            givenSaleContext(asado, account);
            when(stockCutRepository.findAllById(List.of(100L, 101L))).thenReturn(List.of(
                    unit(100L, asado, "8.00", null), unit(101L, asado, "9.50", null)));
            when(saleRepository.save(any(Sale.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(stockCutRepository.claim(any(Sale.class), eq(List.of(100L, 101L)))).thenReturn(2);

            // Act
            service.createSale(request(1L, List.of(100L, 101L)));

            // Assert
            verify(saleRepository).save(saleCaptor.capture());
            Sale sale = saleCaptor.getValue();
            assertThat(sale.getUnit()).isEqualTo(2);
            assertThat(sale.getKilograms()).isEqualByComparingTo("17.500");
            assertThat(sale.getAmount()).isEqualByComparingTo("1750.00");
            assertThat(sale.getBalance()).isEqualByComparingTo("2750.00");
            verify(balanceRecalculationService).recalculate(account);
        }

        @Test
        @DisplayName("Crea la venta aunque la cuenta todavía no tenga saldo")
        void createSaleWorksWhenTheAccountHasNoBalanceYet() {

            // Arrange: cuenta sin saldo (null)
            CutMeat asado = cutMeat(1L, "Asado", "100.00", true);
            givenSaleContext(asado, account(null));
            when(stockCutRepository.findAllById(List.of(100L))).thenReturn(List.of(unit(100L, asado, "8.00", null)));
            when(saleRepository.save(any(Sale.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(stockCutRepository.claim(any(Sale.class), eq(List.of(100L)))).thenReturn(1);

            // Act
            service.createSale(request(1L, List.of(100L)));

            // Assert: el saldo arranca de cero
            verify(saleRepository).save(saleCaptor.capture());
            assertThat(saleCaptor.getValue().getBalance()).isEqualByComparingTo("800.00");
        }

        @Test
        @DisplayName("Rechaza una venta sin unidades")
        void createSaleRejectsASaleWithoutUnits() {

            // Arrange
            givenSaleContext(cutMeat(1L, "Asado", "100.00", true), account("0.00"));

            // Act + Assert
            assertThatThrownBy(() -> service.createSale(request(1L, List.of())))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("al menos una unidad");

            verify(saleRepository, never()).save(any());
        }

        @Test
        @DisplayName("Rechaza elegir dos veces la misma unidad")
        void createSaleRejectsTheSameUnitTwice() {

            // Arrange
            givenSaleContext(cutMeat(1L, "Asado", "100.00", true), account("0.00"));

            // Act + Assert
            assertThatThrownBy(() -> service.createSale(request(1L, List.of(100L, 100L))))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("más de una vez");

            verify(saleRepository, never()).save(any());
        }

        @Test
        @DisplayName("Rechaza una unidad que no existe (o es de otro usuario)")
        void createSaleRejectsAUnitThatDoesNotExist() {

            // Arrange: se piden dos unidades y el repositorio devuelve una sola
            CutMeat asado = cutMeat(1L, "Asado", "100.00", true);
            givenSaleContext(asado, account("0.00"));
            when(stockCutRepository.findAllById(List.of(100L, 101L))).thenReturn(List.of(unit(100L, asado, "8.00", null)));

            // Act + Assert
            assertThatThrownBy(() -> service.createSale(request(1L, List.of(100L, 101L))))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(saleRepository, never()).save(any());
        }

        @Test
        @DisplayName("Rechaza unidades de un corte distinto al de la venta")
        void createSaleRejectsUnitsOfAnotherCut() {

            // Arrange: la venta es de Asado (1) y la unidad es de Rueda (2)
            CutMeat asado = cutMeat(1L, "Asado", "100.00", true);
            CutMeat rueda = cutMeat(2L, "Rueda", "200.00", true);
            givenSaleContext(asado, account("0.00"));
            when(stockCutRepository.findAllById(List.of(100L))).thenReturn(List.of(unit(100L, rueda, "15.00", null)));

            // Act + Assert
            assertThatThrownBy(() -> service.createSale(request(1L, List.of(100L))))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("no son del corte indicado");

            verify(saleRepository, never()).save(any());
        }

        @Test
        @DisplayName("Rechaza una unidad que ya fue vendida")
        void createSaleRejectsAUnitThatWasAlreadySold() {

            // Arrange: la unidad ya pertenece a la venta 99
            CutMeat asado = cutMeat(1L, "Asado", "100.00", true);
            givenSaleContext(asado, account("0.00"));
            Sale otherSale = Sale.builder().id(99L).build();
            when(stockCutRepository.findAllById(List.of(100L))).thenReturn(List.of(unit(100L, asado, "8.00", otherSale)));

            // Act + Assert
            assertThatThrownBy(() -> service.createSale(request(1L, List.of(100L))))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("ya fue vendida");

            verify(saleRepository, never()).save(any());
        }

        @Test
        @DisplayName("Falla si otra venta se llevó una unidad mientras tanto y no recalcula el saldo")
        void createSaleFailsWhenAnotherSaleTookTheUnitFirst() {

            // Arrange: las dos unidades estaban libres al leerlas, pero la reserva solo alcanza una
            CutMeat asado = cutMeat(1L, "Asado", "100.00", true);
            givenSaleContext(asado, account("0.00"));
            when(stockCutRepository.findAllById(List.of(100L, 101L))).thenReturn(List.of(
                    unit(100L, asado, "8.00", null), unit(101L, asado, "9.50", null)));
            when(saleRepository.save(any(Sale.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(stockCutRepository.claim(any(Sale.class), eq(List.of(100L, 101L)))).thenReturn(1);

            // Act + Assert
            assertThatThrownBy(() -> service.createSale(request(1L, List.of(100L, 101L))))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("se acaba de vender");

            verify(balanceRecalculationService, never()).recalculate(any());
        }

        @Test
        @DisplayName("Rechaza vender un corte eliminado")
        void createSaleRejectsADeletedCut() {

            // Arrange
            Client client = Client.builder().id(1L).build();
            when(clientRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(client));
            when(cutMeatRepository.findById(1L)).thenReturn(Optional.of(cutMeat(1L, "Asado", "100.00", false)));

            // Act + Assert
            assertThatThrownBy(() -> service.createSale(request(1L, List.of(100L))))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("fue eliminado");

            verify(saleRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("updateSale()")
    class UpdateSaleTests {

        @Test
        @DisplayName("Conserva las unidades que siguen, libera las que se sacaron y reserva las nuevas")
        void updateSaleKeepsReleasesAndClaimsUnits() {

            // Arrange: la venta tiene la unidad 100 (8 kg) y se cambia por la 100 + la 102 (10 kg)
            CutMeat asado = cutMeat(1L, "Asado", "100.00", true);
            Sale sale = existingSale(asado, account("1000.00"));
            when(saleRepository.findById(5L)).thenReturn(Optional.of(sale));
            when(cutMeatRepository.findById(1L)).thenReturn(Optional.of(asado));
            when(stockCutRepository.findAllById(List.of(100L, 102L))).thenReturn(List.of(
                    unit(100L, asado, "8.00", sale), unit(102L, asado, "10.00", null)));
            when(stockCutRepository.claim(sale, List.of(102L))).thenReturn(1);
            when(saleRepository.saveAndFlush(sale)).thenReturn(sale);

            // Act
            service.updateSale(5L, request(1L, List.of(100L, 102L)));

            // Assert: solo se reserva la nueva; nada de lo elegido se libera
            verify(stockCutRepository).releaseExcept(5L, List.of(100L, 102L));
            verify(stockCutRepository).claim(sale, List.of(102L));
            assertThat(sale.getUnit()).isEqualTo(2);
            assertThat(sale.getKilograms()).isEqualByComparingTo("18.000");
            assertThat(sale.getAmount()).isEqualByComparingTo("1800.00");
            verify(balanceRecalculationService).recalculate(sale.getCurrentAccount());
        }

        @Test
        @DisplayName("Rechaza una unidad que pertenece a otra venta")
        void updateSaleRejectsAUnitOfAnotherSale() {

            // Arrange
            CutMeat asado = cutMeat(1L, "Asado", "100.00", true);
            Sale sale = existingSale(asado, account("1000.00"));
            when(saleRepository.findById(5L)).thenReturn(Optional.of(sale));
            when(cutMeatRepository.findById(1L)).thenReturn(Optional.of(asado));
            when(stockCutRepository.findAllById(List.of(102L))).thenReturn(List.of(
                    unit(102L, asado, "10.00", Sale.builder().id(99L).build())));

            // Act + Assert
            assertThatThrownBy(() -> service.updateSale(5L, request(1L, List.of(102L))))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("ya fue vendida");

            verify(stockCutRepository, never()).releaseExcept(any(), any());
            verify(saleRepository, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("Al cambiar de corte actualiza el nombre y el precio de la venta")
        void updateSaleRefreshesTheSnapshotWhenTheCutChanges() {

            // Arrange: la venta pasa de Asado ($100) a Rueda ($200) con una unidad de 15 kg
            CutMeat asado = cutMeat(1L, "Asado", "100.00", true);
            CutMeat rueda = cutMeat(2L, "Rueda", "200.00", true);
            Sale sale = existingSale(asado, account("1000.00"));
            when(saleRepository.findById(5L)).thenReturn(Optional.of(sale));
            when(cutMeatRepository.findById(2L)).thenReturn(Optional.of(rueda));
            when(stockCutRepository.findAllById(List.of(200L))).thenReturn(List.of(unit(200L, rueda, "15.00", null)));
            when(stockCutRepository.claim(sale, List.of(200L))).thenReturn(1);
            when(saleRepository.saveAndFlush(sale)).thenReturn(sale);

            // Act
            service.updateSale(5L, request(2L, List.of(200L)));

            // Assert
            assertThat(sale.getCutMeat()).isSameAs(rueda);
            assertThat(sale.getCutMeatName()).isEqualTo("Rueda");
            assertThat(sale.getPricePerKilo()).isEqualByComparingTo("200.00");
            assertThat(sale.getAmount()).isEqualByComparingTo("3000.00");
        }

        @Test
        @DisplayName("Conserva el precio guardado si el corte no cambia, aunque el precio actual sea otro")
        void updateSaleKeepsTheStoredPriceWhenTheCutDoesNotChange() {

            // Arrange: el Asado hoy cuesta $150, pero la venta se hizo a $100
            CutMeat asadoToday = cutMeat(1L, "Asado", "150.00", true);
            Sale sale = existingSale(cutMeat(1L, "Asado", "100.00", true), account("1000.00"));
            when(saleRepository.findById(5L)).thenReturn(Optional.of(sale));
            when(cutMeatRepository.findById(1L)).thenReturn(Optional.of(asadoToday));
            when(stockCutRepository.findAllById(List.of(100L))).thenReturn(List.of(unit(100L, asadoToday, "8.00", sale)));
            when(saleRepository.saveAndFlush(sale)).thenReturn(sale);

            // Act
            service.updateSale(5L, request(1L, List.of(100L)));

            // Assert
            assertThat(sale.getPricePerKilo()).isEqualByComparingTo("100.00");
            assertThat(sale.getAmount()).isEqualByComparingTo("800.00");
        }

        @Test
        @DisplayName("Rechaza cambiar a un corte eliminado")
        void updateSaleRejectsChangingToADeletedCut() {

            // Arrange
            Sale sale = existingSale(cutMeat(1L, "Asado", "100.00", true), account("1000.00"));
            when(saleRepository.findById(5L)).thenReturn(Optional.of(sale));
            when(cutMeatRepository.findById(2L)).thenReturn(Optional.of(cutMeat(2L, "Rueda", "200.00", false)));

            // Act + Assert
            assertThatThrownBy(() -> service.updateSale(5L, request(2L, List.of(200L))))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("fue eliminado");

            verify(stockCutRepository, never()).releaseExcept(any(), any());
        }

    }

    @Nested
    @DisplayName("deleteSaleById()")
    class DeleteSaleTests {

        @Test
        @DisplayName("Libera las unidades antes de borrar la venta y recalcula el saldo")
        void deleteSaleReleasesTheUnitsBeforeDeleting() {

            // Arrange
            Sale sale = existingSale(cutMeat(1L, "Asado", "100.00", true), account("1000.00"));
            when(saleRepository.findById(5L)).thenReturn(Optional.of(sale));

            // Act
            service.deleteSaleById(5L);

            // Assert: la clave foránea exige liberar primero
            InOrder order = inOrder(stockCutRepository, saleRepository);
            order.verify(stockCutRepository).releaseAll(5L);
            order.verify(saleRepository).delete(sale);
            verify(balanceRecalculationService).recalculate(sale.getCurrentAccount());
        }

        @Test
        @DisplayName("Rechaza una venta que no existe y no libera nada")
        void deleteSaleRejectsASaleThatDoesNotExist() {

            // Arrange
            when(saleRepository.findById(99L)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> service.deleteSaleById(99L))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(stockCutRepository, never()).releaseAll(any());
        }

    }

    @Nested
    @DisplayName("getSaleById()")
    class GetSaleTests {

        @Test
        @DisplayName("Devuelve la venta con sus unidades de stock")
        void getSaleByIdIncludesTheStockCuts() {

            // Arrange
            Sale sale = existingSale(cutMeat(1L, "Asado", "100.00", true), account("1000.00"));
            AvailableCutDto detail = new AvailableCutDto(100L, 1L, "Asado", new BigDecimal("8.00"), 3L, 1, 2,
                    LocalDate.of(2026, 10, 1), "Ternero hasta 110kg", "Frigorifico Sur");
            when(saleRepository.findById(5L)).thenReturn(Optional.of(sale));
            when(stockCutRepository.findDetailsBySaleId(5L)).thenReturn(List.of(detail));

            // Act
            SaleResponseDto response = service.getSaleById(5L);

            // Assert
            assertThat(response.getStockCuts()).containsExactly(detail);
        }

        @Test
        @DisplayName("Rechaza una venta que no existe")
        void getSaleByIdRejectsASaleThatDoesNotExist() {

            // Arrange
            when(saleRepository.findById(99L)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> service.getSaleById(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

    }

}