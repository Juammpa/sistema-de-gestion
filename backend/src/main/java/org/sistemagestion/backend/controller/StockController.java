package org.sistemagestion.backend.controller;

import lombok.RequiredArgsConstructor;
import org.sistemagestion.backend.dto.stock.StockResponseDto;
import org.sistemagestion.backend.service.stock.IStockService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@CrossOrigin(origins = "http://localhost:4200")
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

}
