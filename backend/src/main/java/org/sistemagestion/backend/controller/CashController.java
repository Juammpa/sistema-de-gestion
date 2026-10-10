package org.sistemagestion.backend.controller;

import lombok.RequiredArgsConstructor;
import org.sistemagestion.backend.dto.cash.CashDayDto;
import org.sistemagestion.backend.service.cash.ICashService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/cash")
@RequiredArgsConstructor
public class CashController {

    private final ICashService cashService;

    @GetMapping
    public ResponseEntity<List<CashDayDto>> getCash(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate to) {

        return ResponseEntity.ok(cashService.getCashDays(from, to));
    }

    @GetMapping("/balance")
    public ResponseEntity<BigDecimal> getBalance() {

        return ResponseEntity.ok(cashService.getCurrentBalance());
    }
}
