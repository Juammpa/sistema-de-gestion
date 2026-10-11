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
import org.sistemagestion.backend.model.enums.PurchaseType;
import org.sistemagestion.backend.repository.CurrentAccountRepository;
import org.sistemagestion.backend.repository.PurchasePieceWeightRepository;
import org.sistemagestion.backend.repository.PurchaseRepository;
import org.sistemagestion.backend.repository.SupplierRepository;
import org.sistemagestion.backend.service.account.AccountGuard;
import org.sistemagestion.backend.service.movement.BalanceRecalculationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class PurchaseService implements IPurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final PurchasePieceWeightRepository pieceWeightRepository;
    private final CurrentAccountRepository currentAccountRepository;
    private final SupplierRepository supplierRepository;
    private final BalanceRecalculationService balanceRecalculationService;
    private final AccountGuard accountGuard;
    private final PurchaseMapper mapper;

    @Override
    @Transactional
    public PurchaseResponseDto createPurchase(PurchaseRequestDto request) {

        CurrentAccount currentAccount = currentAccountRepository.findById(request.getCurrentAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("CurrentAccount","id",request.getCurrentAccountId()));

        accountGuard.assertOwnerActive(currentAccount);
        assertSupplierCanSell(currentAccount, request.getPurchaseType());

        Purchase purchase = mapper.toPurchase(request, currentAccount);
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
        assertSupplierCanSell(purchase.getCurrentAccount(), request.getPurchaseType());

        boolean distributionChanged = !Decimals.sameValue(purchase.getKilograms(), request.getKilograms()) ||
                !Objects.equals(purchase.getUnit(), request.getUnit());

        purchase.setDate(request.getDate());
        purchase.setPurchaseType(request.getPurchaseType());
        purchase.setKilograms(Decimals.kilograms(request.getKilograms()));
        purchase.setPricePerKilo(Decimals.money(request.getPricePerKilo()));
        purchase.setUnit(request.getUnit());
        purchase.setAmount(Decimals.multiply(request.getKilograms(), request.getPricePerKilo()));

        if (distributionChanged) {
            pieceWeightRepository.deleteByPurchaseId(id);
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

        // Actualizamos el balance de la cuenta
        CurrentAccount currentAccount = purchase.getCurrentAccount();
        pieceWeightRepository.deleteByPurchaseId(id);
        purchaseRepository.delete(purchase);
        purchaseRepository.flush();

        balanceRecalculationService.recalculate(currentAccount);

        log.info("Compra eliminada correctamente. ID: {}", purchase.getId());

    }

    // El tipo de proveedor (bovinos / porcinos / ambos) limita qué tipos de compra puede vender
    private void assertSupplierCanSell(CurrentAccount account, PurchaseType purchaseType) {
        supplierRepository.findByCurrentAccountId(account.getId())
                .filter(supplier -> !supplier.getSupplierType().canSell(purchaseType))
                .ifPresent(supplier -> {
                    throw new BusinessRuleException(String.format(
                            "Este proveedor solo trabaja con %s: no se puede registrar ese tipo de compra.",
                            supplier.getSupplierType().getLabel()
                    ));
                });
    }

}
