package org.sistemagestion.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sistemagestion.backend.dto.sales.SaleRequestDto;
import org.sistemagestion.backend.dto.sales.SaleResponseDto;
import org.sistemagestion.backend.service.sale.ISaleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("api/sales")
@RequiredArgsConstructor
public class SaleController {

    private final ISaleService saleService;

    @PostMapping
    public ResponseEntity<SaleResponseDto> createSale(@Valid @RequestBody SaleRequestDto request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(saleService.createSale(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SaleResponseDto> getSaleById(@PathVariable Long id) {

        return ResponseEntity.ok(saleService.getSaleById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SaleResponseDto> updateSale(@PathVariable Long id, @Valid @RequestBody SaleRequestDto request) {

        return ResponseEntity.ok(saleService.updateSale(id, request));
    }

    @GetMapping("/all")
    public ResponseEntity<List<SaleResponseDto>> getAllSales() {

        return ResponseEntity.ok(saleService.getAllSales());
    }

    @GetMapping
    public ResponseEntity<List<SaleResponseDto>> getAllSalesByClient(@RequestParam Long clientId) {

        return ResponseEntity.ok(saleService.getAllSalesByClient(clientId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSaleById(@PathVariable Long id) {

        saleService.deleteSaleById(id);
        return ResponseEntity.noContent().build();
    }




}
