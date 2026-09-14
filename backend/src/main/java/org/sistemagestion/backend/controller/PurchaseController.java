package org.sistemagestion.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sistemagestion.backend.dto.purchase.PurchaseRequestDto;
import org.sistemagestion.backend.dto.purchase.PurchaseResponseDto;
import org.sistemagestion.backend.service.purchase.IPurchaseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/purchase")
@RequiredArgsConstructor
public class PurchaseController {

    private final IPurchaseService purchaseService;

    @PostMapping
    public ResponseEntity<PurchaseResponseDto> createPurchase(@Valid @RequestBody PurchaseRequestDto request){

        return ResponseEntity.status(HttpStatus.CREATED).body(purchaseService.createPurchase(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseResponseDto> getPurchaseById(@PathVariable Long id) {

        return ResponseEntity.ok(purchaseService.getPurchaseById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PurchaseResponseDto> updatePurchase(@PathVariable Long id, @Valid @RequestBody PurchaseRequestDto request) {

        return ResponseEntity.ok(purchaseService.updatePurchase(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {

        purchaseService.deletePurchaseById(id);
        return ResponseEntity.noContent().build();
    }

}
