package org.sistemagestion.backend.service.purchase;

import org.sistemagestion.backend.dto.purchase.PurchaseRequestDto;
import org.sistemagestion.backend.dto.purchase.PurchaseResponseDto;

import java.util.List;

public interface IPurchaseService {

    PurchaseResponseDto createPurchase(PurchaseRequestDto request);
    PurchaseResponseDto getPurchaseById(Long id);
    PurchaseResponseDto updatePurchase(Long id, PurchaseRequestDto request);
    void deletePurchaseById(Long id);
}
