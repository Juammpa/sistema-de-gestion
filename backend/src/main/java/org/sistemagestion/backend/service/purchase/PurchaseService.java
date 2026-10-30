package org.sistemagestion.backend.service.purchase;

import lombok.RequiredArgsConstructor;
import org.sistemagestion.backend.common.Decimals;
import lombok.extern.slf4j.Slf4j;
import org.sistemagestion.backend.dto.purchase.PurchaseRequestDto;
import org.sistemagestion.backend.dto.purchase.PurchaseResponseDto;
import org.sistemagestion.backend.exception.BusinessRuleException;
import org.sistemagestion.backend.exception.ResourceNotFoundException;
import org.sistemagestion.backend.mapper.PurchaseMapper;
import org.sistemagestion.backend.model.CurrentAccount;
import org.sistemagestion.backend.model.Purchase;
import org.sistemagestion.backend.model.PurchaseType;
import org.sistemagestion.backend.repository.*;
import org.sistemagestion.backend.service.account.AccountGuard;
import org.sistemagestion.backend.service.movement.BalanceRecalculationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class PurchaseService implements IPurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final PurchaseTypeRepository purchaseTypeRepository;
    private final PurchasePieceWeightRepository pieceWeightRepository;
    private final CurrentAccountRepository currentAccountRepository;
    private final SupplierRepository supplierRepository;
    private final StockCutRepository stockCutRepository;
    private final StockPieceClosureRepository stockPieceClosureRepository;
    private final BalanceRecalculationService balanceRecalculationService;
    private final AccountGuard accountGuard;
    private final PurchaseMapper mapper;

    @Override
    @Transactional
    public PurchaseResponseDto createPurchase(PurchaseRequestDto request) {

        CurrentAccount currentAccount = currentAccountRepository.findById(request.getCurrentAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("CurrentAccount","id",request.getCurrentAccountId()));

        accountGuard.assertOwnerActive(currentAccount);
        PurchaseType purchaseType = resolvePurchaseType(request.getPurchaseTypeId(), null);
        assertSupplierCanSell(currentAccount, purchaseType);

        Purchase purchase = mapper.toPurchase(request, currentAccount, purchaseType);
        Purchase purchaseSaved = purchaseRepository.save(purchase);

        balanceRecalculationService.recalculate(currentAccount);

        log.info("Compra creada correctamente. ID: {}", purchaseSaved.getId());
        return mapper.toDto(purchaseSaved);
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseResponseDto getPurchaseById(Long id) {
        return purchaseRepository.findById(id)
                .map(mapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase","id",id));
    }

    @Override
    @Transactional
    public PurchaseResponseDto updatePurchase(Long id, PurchaseRequestDto request) {

        Purchase purchase = purchaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase","id", id));

        accountGuard.assertOwnerActive(purchase.getCurrentAccount());
        PurchaseType purchaseType = resolvePurchaseType(request.getPurchaseTypeId(), purchase.getPurchaseType());
        assertSupplierCanSell(purchase.getCurrentAccount(), purchaseType);

        boolean distributionChanged = !Decimals.sameValue(purchase.getKilograms(), request.getKilograms()) ||
                !Objects.equals(purchase.getUnit(), request.getUnit());

        boolean specieChanged = purchase.getPurchaseType().getSpecie() != purchaseType.getSpecie();

        // Los cortes cuelgan de las piezas y de la especie: cambiarlas los dejaria inconsistentes
        if ((distributionChanged || specieChanged) && stockCutRepository.existsByPurchaseId(id)) {
            throw new BusinessRuleException(
                    "La compra tiene cortes: quitalos del stock antes de cambiar sus kilos, unidades o especie."
            );
        }

        purchase.setDate(request.getDate());
        purchase.setPurchaseType(purchaseType);
        purchase.setSpecie(purchaseType.getSpecie());
        purchase.setKilograms(Decimals.kilograms(request.getKilograms()));
        purchase.setPricePerKilo(Decimals.money(request.getPricePerKilo()));
        purchase.setUnit(request.getUnit());
        purchase.setAmount(Decimals.multiply(request.getKilograms(), request.getPricePerKilo()));

        if (distributionChanged) {
            pieceWeightRepository.deleteByPurchaseId(id);
            stockPieceClosureRepository.deleteByPurchaseId(id);
        }

        Purchase purchaseSaved = purchaseRepository.saveAndFlush(purchase);

        balanceRecalculationService.recalculate(purchase.getCurrentAccount());

        log.info("Compra actualizada correctamente. ID: {}", purchaseSaved.getId());
        return mapper.toDto(purchaseSaved);
    }

    @Override
    @Transactional
    public void deletePurchaseById(Long id) {

        Purchase purchase = purchaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase","id", id));

        if (stockCutRepository.existsByPurchaseId(id)) {
            throw new BusinessRuleException(
                    "La compra tiene cortes: quitalos del stock antes de eliminarla."
            );
        }

        // Actualizamos el balance de la cuenta
        CurrentAccount currentAccount = purchase.getCurrentAccount();
        pieceWeightRepository.deleteByPurchaseId(id);
        stockPieceClosureRepository.deleteByPurchaseId(id);
        purchaseRepository.delete(purchase);
        purchaseRepository.flush();

        balanceRecalculationService.recalculate(currentAccount);

        log.info("Compra eliminada correctamente. ID: {}", purchase.getId());

    }

    // El tipo debe ser del usuario y estar activo
    // Al editar una compra vieja se tolera su tipo actual aunque haya sido archivado (borrado logico)
    private PurchaseType resolvePurchaseType(Long id, PurchaseType current) {

        PurchaseType type = purchaseTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseType","id",id));

        boolean keepsCurrentType = current != null && Objects.equals(current.getId(), type.getId());

        if (!type.getIsActive() &&  !keepsCurrentType) {
            throw new BusinessRuleException("Ese tipo de compra fue eliminado: elegí otro");
        }

        return  type;
    }

    // El tipo de proveedor (bovinos / porcinos / ambos) limita qué especies puede vender
    private void assertSupplierCanSell(CurrentAccount account, PurchaseType purchaseType) {
        supplierRepository.findByCurrentAccountId(account.getId())
                .filter(supplier -> !supplier.getSupplierType().canSell(purchaseType.getSpecie()))
                .ifPresent(supplier -> {
                    throw new BusinessRuleException(String.format(
                            "Este proveedor solo trabaja con %s: no se puede registrar ese tipo de compra.",
                            supplier.getSupplierType().getLabel()
                    ));
                });
    }

}
