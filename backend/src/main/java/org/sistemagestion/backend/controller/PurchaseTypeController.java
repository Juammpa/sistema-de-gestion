package org.sistemagestion.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sistemagestion.backend.dto.purchase_type.PurchaseTypeRequestDto;
import org.sistemagestion.backend.dto.purchase_type.PurchaseTypeResponseDto;
import org.sistemagestion.backend.model.enums.Specie;
import org.sistemagestion.backend.service.purchase_type.IPurchaseTypeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/purchase-types")
@RequiredArgsConstructor
public class PurchaseTypeController {

    private final IPurchaseTypeService purchaseTypeService;

    @PostMapping
    public ResponseEntity<PurchaseTypeResponseDto> create(@Valid @RequestBody PurchaseTypeRequestDto request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(purchaseTypeService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseTypeResponseDto> getById(@PathVariable Long id) {

        return ResponseEntity.ok(purchaseTypeService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<PurchaseTypeResponseDto>> getAll(@RequestParam(required = false) Specie specie) {

        return ResponseEntity.ok(purchaseTypeService.getAll(specie));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PurchaseTypeResponseDto> update(@PathVariable Long id, @Valid @RequestBody PurchaseTypeRequestDto request) {

        return ResponseEntity.ok(purchaseTypeService.update(id, request));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activate(@PathVariable Long id) {

        purchaseTypeService.activate(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        purchaseTypeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> exists(@RequestParam String name, @RequestParam Specie specie, @RequestParam(required = false) Long excludeId) {

        return ResponseEntity.ok(purchaseTypeService.existsByNameAndSpecie(name, specie, excludeId));
    }
}
