package org.sistemagestion.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sistemagestion.backend.dto.butcher_shop.ButcherShopRequestDto;
import org.sistemagestion.backend.dto.butcher_shop.ButcherShopResponseDto;
import org.sistemagestion.backend.dto.butcher_shop.ButcherShopUpdateRequestDto;
import org.sistemagestion.backend.service.butcher_shop.IButcherShopService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/butcher-shops")
@RequiredArgsConstructor
public class ButcherShopController {

    private final IButcherShopService butcherShopService;

    @PostMapping
    public ResponseEntity<ButcherShopResponseDto> createButcherShop(@Valid @RequestBody ButcherShopRequestDto request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(butcherShopService.createButcherShop(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ButcherShopResponseDto> getById(@PathVariable Long id) {

        return ResponseEntity.ok(butcherShopService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ButcherShopResponseDto> updateButcherShop(@PathVariable Long id, @Valid @RequestBody ButcherShopUpdateRequestDto request) {

        return ResponseEntity.ok(butcherShopService.updateButcherShop(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteButcherShop(@PathVariable Long id) {

        butcherShopService.deleteButcherShop(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> existsByName(@RequestParam Long clientId, @RequestParam String name, @RequestParam(required = false) Long excludeId) {

        return ResponseEntity.ok(butcherShopService.existsByName(clientId, name, excludeId));
    }

}
