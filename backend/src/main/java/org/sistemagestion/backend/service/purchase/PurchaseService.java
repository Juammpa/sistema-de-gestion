package org.sistemagestion.backend.service.purchase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sistemagestion.backend.dto.purchase.PurchaseRequestDto;
import org.sistemagestion.backend.dto.purchase.PurchaseResponseDto;
import org.sistemagestion.backend.exception.ResourceNotFoundException;
import org.sistemagestion.backend.mapper.PurchaseMapper;
import org.sistemagestion.backend.model.CurrentAccount;
import org.sistemagestion.backend.model.Purchase;
import org.sistemagestion.backend.repository.CurrentAccountRepository;
import org.sistemagestion.backend.repository.PurchaseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PurchaseService implements IPurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final CurrentAccountRepository currentAccountRepository;
    private final PurchaseMapper mapper;

    @Override
    @Transactional
    public PurchaseResponseDto createPurchase(PurchaseRequestDto request) {

        CurrentAccount currentAccount = currentAccountRepository.findById(request.getCurrentAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("CurrentAccount","id",request.getCurrentAccountId()));

        Purchase purchase = mapper.toPurchase(request, currentAccount);
        Purchase purchaseSaved = purchaseRepository.save(purchase);
        Double amount = request.getKilograms() * request.getPricePerKilo();

        currentAccount.setCurrentBalance(currentAccount.getCurrentBalance() + amount);

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

        CurrentAccount currentAccount = purchase.getCurrentAccount();
        Double oldAmount = purchase.getAmount();
        Double newAmount = request.getKilograms() * request.getPricePerKilo();
        Double amountDifference = newAmount - oldAmount;

        purchase.setPurchaseType(request.getPurchaseType());
        purchase.setKilograms(request.getKilograms());
        purchase.setPricePerKilo(request.getPricePerKilo());
        purchase.setUnit(request.getUnit());
        purchase.setAmount(newAmount);
        purchase.setBalance(purchase.getBalance() + amountDifference);

        currentAccount.setCurrentBalance(currentAccount.getCurrentBalance() + amountDifference);

        Purchase purchaseSaved = purchaseRepository.save(purchase);
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
        currentAccount.setCurrentBalance(currentAccount.getCurrentBalance() - purchase.getAmount());

        purchaseRepository.delete(purchase);
        log.info("Compra eliminada correctamente. ID: {}", purchase.getId());

    }
}
