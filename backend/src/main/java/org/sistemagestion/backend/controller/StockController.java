package org.sistemagestion.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sistemagestion.backend.dto.stock.AvailableCutDto;
import org.sistemagestion.backend.dto.stock.StockCutsRequestDto;
import org.sistemagestion.backend.dto.stock.StockResponseDto;
import org.sistemagestion.backend.dto.stock.StockWeightsRequestDto;
import org.sistemagestion.backend.service.stock.IStockService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
public class StockController {

    private final IStockService stockService;

    @GetMapping
    public ResponseEntity<StockResponseDto> getStock(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to)
    {
        return ResponseEntity.ok(stockService.getStock(from, to));
    }

    @PutMapping("/purchase/{purchaseId}/weights")
    public ResponseEntity<Void> updatePieceWeights(@PathVariable Long purchaseId, @Valid @RequestBody StockWeightsRequestDto request) {

        stockService.updatePieceWeights(purchaseId, request.kilograms());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/purchase/{purchaseId}/pieces/{pieceNumber}/cuts")
    public ResponseEntity<Void> updatePieceCuts(@PathVariable Long purchaseId, @PathVariable int pieceNumber, @Valid @RequestBody StockCutsRequestDto request) {

        stockService.updatePieceCuts(purchaseId, pieceNumber, request.cuts());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/cuts/available")
    public ResponseEntity<List<AvailableCutDto>> getAvailableCuts() {

        return ResponseEntity.ok(stockService.getAvailableCuts());
    }
}
