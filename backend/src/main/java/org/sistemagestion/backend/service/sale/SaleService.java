package org.sistemagestion.backend.service.sale;

import lombok.RequiredArgsConstructor;
import org.sistemagestion.backend.common.Decimals;
import lombok.extern.slf4j.Slf4j;
import org.sistemagestion.backend.dto.sales.SaleRequestDto;
import org.sistemagestion.backend.dto.sales.SaleResponseDto;
import org.sistemagestion.backend.exception.BusinessRuleException;
import org.sistemagestion.backend.exception.ResourceNotFoundException;
import org.sistemagestion.backend.mapper.SaleMapper;
import org.sistemagestion.backend.model.*;
import org.sistemagestion.backend.repository.ButcherShopRepository;
import org.sistemagestion.backend.repository.ClientRepository;
import org.sistemagestion.backend.repository.CutMeatRepository;
import org.sistemagestion.backend.repository.SaleRepository;
import org.sistemagestion.backend.repository.StockCutRepository;
import org.sistemagestion.backend.service.account.AccountGuard;
import org.sistemagestion.backend.service.movement.BalanceRecalculationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SaleService implements ISaleService{

    private final SaleRepository saleRepository;
    private final ClientRepository clientRepository;
    private final CutMeatRepository cutMeatRepository;
    private final ButcherShopRepository butcherShopRepository;
    private final StockCutRepository stockCutRepository;
    private final BalanceRecalculationService balanceRecalculationService;
    private final AccountGuard accountGuard;
    private final SaleMapper mapper;

    @Override
    @Transactional
    public SaleResponseDto createSale(SaleRequestDto request) {

        Client client = clientRepository.findByIdAndIsActiveTrue(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client","id", request.getClientId()));

        CutMeat cutMeat = cutMeatRepository.findById(request.getCutMeatId())
                .orElseThrow(() -> new ResourceNotFoundException("CutMeat", "id", request.getCutMeatId()));

        assertCutMeatActive(cutMeat);

        ButcherShop butcherShop = butcherShopRepository.findByIdAndIsActiveTrue(request.getButcherShopId())
                .orElseThrow(() -> new ResourceNotFoundException("ButcherShop", "id", request.getButcherShopId()));

        // Validamos si la sucursal es del cliente
        if (!butcherShop.getClient().getId().equals(client.getId())) {
            throw new BusinessRuleException("La sucursal no pertenece al cliente indicado.");
        }

        // Obtenemos cuenta corriente de la sucursal
        CurrentAccount currentAccount = butcherShop.getCurrentAccount();

        // Solo se vende lo que hay en stock: los kilos y las unidades salen de las unidades elegidas
        List<StockCut> units = loadUnits(request.getStockCutIds(), cutMeat, null);

        Sale sale = mapper.toSale(client, cutMeat, currentAccount, totalKilograms(units), units.size());
        Sale saleSaved = saleRepository.save(sale);

        // Si otra venta se llevó alguna unidad antes, falla y se deshace también la venta recién guardada
        claim(saleSaved, units);

        balanceRecalculationService.recalculate(currentAccount);

        log.info("Venta creada correctamente. ID: {}", saleSaved.getId());
        return withStockCuts(mapper.toDto(saleSaved));
    }

    @Override
    @Transactional(readOnly = true)
    public SaleResponseDto getSaleById(Long id) {
        return saleRepository.findById(id)
                .map(sale -> withStockCuts(mapper.toDto(sale)))
                .orElseThrow(() -> new ResourceNotFoundException("Sale", "id", id));
    }

    @Override
    @Transactional
    public SaleResponseDto updateSale(Long id, SaleRequestDto request) {

        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale","id",id));

        accountGuard.assertOwnerActive(sale.getCurrentAccount());

        CutMeat cutMeat = cutMeatRepository.findById(request.getCutMeatId())
                .orElseThrow(() -> new ResourceNotFoundException("CutMeat","id", request.getCutMeatId()));

        // Se puede conservar el corte original aunque ya este eliminado, pero no elegir uno eliminado
        boolean cutChanged = !cutMeat.getId().equals(sale.getCutMeat().getId());

        // Nombre y precio son una foto del momento de la venta: solo se actualiza si se cambia de corte
        if (cutChanged) {
            assertCutMeatActive(cutMeat);

            sale.setCutMeat(cutMeat);
            sale.setCutMeatName(cutMeat.getCutMeatName());
            sale.setPricePerKilo(cutMeat.getPricePerKilo());
        }

        // Las unidades elegidas definen los kilos y las unidades de la venta: las que ya no se eligen
        // se liberan y las nuevas se reservan
        List<StockCut> units = loadUnits(request.getStockCutIds(), cutMeat, id);

        stockCutRepository.releaseExcept(id, units.stream().map(StockCut::getId).toList());
        claim(sale, units);

        sale.setDate(request.getDate());
        sale.setUnit(units.size());
        sale.setKilograms(Decimals.kilograms(totalKilograms(units)));
        sale.setAmount(Decimals.multiply(sale.getKilograms(), sale.getPricePerKilo()));

        Sale saleUpdated = saleRepository.saveAndFlush(sale);

        balanceRecalculationService.recalculate(sale.getCurrentAccount());

        log.info("Venta actualizada correctamente. ID: {}", sale.getId());
        return withStockCuts(mapper.toDto(saleUpdated));

    }

    @Override
    @Transactional(readOnly = true)
    public List<SaleResponseDto> getAllSales() {
        return saleRepository.findAll().stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SaleResponseDto> getAllSalesByClient(Long clientId) {
        return saleRepository.findByClientId(clientId).stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void deleteSaleById(Long id) {

        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale", "id", id));

        // Las unidades de la venta vuelven a estar disponibles antes de borrarla
        stockCutRepository.releaseAll(id);

        // Actualizamos balance de la cuenta
        CurrentAccount currentAccount = sale.getCurrentAccount();
        saleRepository.delete(sale);
        saleRepository.flush();

        balanceRecalculationService.recalculate(currentAccount);
        log.info("Venta eliminada correctamente. ID: {}", sale.getId());
    }

    // ===================== Helpers =====================

    // Un corte eliminado (borrado logico) no se puede vender
    private void assertCutMeatActive(CutMeat cutMeat) {
        if (Boolean.FALSE.equals(cutMeat.getIsActive())) {
            throw new BusinessRuleException(String.format(
                    "El corte \"%s\" fue eliminado y no se puede vender.",
                    cutMeat.getCutMeatName()
            ));
        }
    }

    // Unidades de stock elegidas: tienen que existir, ser del corte de la venta y estar libres
    // (o ya ser de esta misma venta, al editarla)
    private List<StockCut> loadUnits(List<Long> stockCutIds, CutMeat cutMeat, Long saleId) {

        if (stockCutIds == null || stockCutIds.isEmpty()) {
            throw new BusinessRuleException("Elegí al menos una unidad de stock para vender.");
        }

        if (stockCutIds.stream().distinct().count() != stockCutIds.size()) {
            throw new BusinessRuleException("Una unidad no se puede elegir más de una vez.");
        }

        List<StockCut> units = stockCutRepository.findAllById(stockCutIds);

        if (units.size() != stockCutIds.size()) {
            Set<Long> found = units.stream().map(StockCut::getId).collect(Collectors.toSet());
            Long missing = stockCutIds.stream().filter(unitId -> !found.contains(unitId)).findFirst().orElseThrow();
            throw new ResourceNotFoundException("StockCut", "id", missing);
        }

        for (StockCut unit : units) {
            if (!unit.getCutMeat().getId().equals(cutMeat.getId())) {
                throw new BusinessRuleException("Las unidades elegidas no son del corte indicado.");
            }

            if (unit.isSold() && !unit.getSale().getId().equals(saleId)) {
                throw new BusinessRuleException(
                        "Una de las unidades elegidas ya fue vendida: actualizá la lista y volvé a elegir.");
            }
        }

        return units;
    }

    // Reserva las unidades que todavía no son de la venta. Es una sola actualización condicionada a que
    // sigan libres: si otra venta se las llevó entre la lectura y este momento, no se actualizan todas.
    private void claim(Sale sale, List<StockCut> units) {

        List<Long> toClaim = units.stream()
                .filter(unit -> !unit.isSold())
                .map(StockCut::getId)
                .toList();

        if (!toClaim.isEmpty() && stockCutRepository.claim(sale, toClaim) != toClaim.size()) {
            throw new BusinessRuleException(
                    "Una de las unidades se acaba de vender en otra venta: actualizá la lista y volvé a elegir.");
        }
    }

    private BigDecimal totalKilograms(List<StockCut> units) {
        return units.stream().map(StockCut::getKilograms).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private SaleResponseDto withStockCuts(SaleResponseDto dto) {
        dto.setStockCuts(stockCutRepository.findDetailsBySaleId(dto.getId()));
        return dto;
    }

}