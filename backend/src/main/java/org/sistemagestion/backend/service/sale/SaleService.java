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
import org.sistemagestion.backend.service.account.AccountGuard;
import org.sistemagestion.backend.service.movement.BalanceRecalculationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SaleService implements ISaleService{

    private final SaleRepository saleRepository;
    private final ClientRepository clientRepository;
    private final CutMeatRepository cutMeatRepository;
    private final ButcherShopRepository butcherShopRepository;
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

        Sale sale = mapper.toSale(request, client, cutMeat, currentAccount);
        Sale saleSaved = saleRepository.save(sale);

        balanceRecalculationService.recalculate(currentAccount);

        log.info("Venta creada correctamente. ID: {}", saleSaved.getId());
        return mapper.toDto(saleSaved);
    }

    @Override
    @Transactional(readOnly = true)
    public SaleResponseDto getSaleById(Long id) {
        return saleRepository.findById(id)
                .map(mapper::toDto)
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

        // Se puede conservar el corte original aunque ya este eliminado, pero no elegir uno eliminado.
        boolean cutChanged = !cutMeat.getId().equals(sale.getCutMeat().getId());
        if (cutChanged) {
            assertCutMeatActive(cutMeat);
        }

        // Nombre y precio son una foto del momento de la venta: solo se actualiza si se cambia de corte
        // (o si es una venta antigua que no tiene precio guardado)
        if (cutChanged || sale.getPricePerKilo() == null) {
            sale.setCutMeat(cutMeat);
            sale.setCutMeatName(cutMeat.getCutMeatName());
            sale.setPricePerKilo(cutMeat.getPricePerKilo());
        }

        sale.setDate(request.getDate());
        sale.setUnit(request.getUnit());
        sale.setKilograms(Decimals.kilograms(request.getKilograms()));
        sale.setAmount(Decimals.multiply(request.getKilograms(), sale.getPricePerKilo()));

        Sale saleUpdated = saleRepository.saveAndFlush(sale);

        balanceRecalculationService.recalculate(sale.getCurrentAccount());

        log.info("Venta actualizada correctamente. ID: {}", sale.getId());
        return mapper.toDto(saleUpdated);
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

        // Actualizamos balance de la cuenta
        CurrentAccount currentAccount = sale.getCurrentAccount();
        saleRepository.delete(sale);
        saleRepository.flush();

        balanceRecalculationService.recalculate(currentAccount);
        log.info("Venta eliminada correctamente. ID: {}", sale.getId());
    }

    // Un corte eliminado (borrado logico) no se puede vender
    private void assertCutMeatActive(CutMeat cutMeat) {
        if (Boolean.FALSE.equals(cutMeat.getIsActive())) {
            throw new BusinessRuleException(String.format(
                    "El corte \"%s\" fue eliminado y no se puede vender.",
                    cutMeat.getCutMeatName()
            ));
        }
    }

}
