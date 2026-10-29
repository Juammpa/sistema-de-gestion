package org.sistemagestion.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sistemagestion.backend.dto.custom_payment_method.CustomPaymentMethodRequestDto;
import org.sistemagestion.backend.dto.custom_payment_method.CustomPaymentMethodResponseDto;
import org.sistemagestion.backend.service.custom_payment_method.ICustomPaymentMethodService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payment-methods")
@RequiredArgsConstructor
public class CustomPaymentMethodController {

    private final ICustomPaymentMethodService service;

    @PostMapping
    public ResponseEntity<CustomPaymentMethodResponseDto> create(@Valid @RequestBody CustomPaymentMethodRequestDto request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomPaymentMethodResponseDto> getById(@PathVariable Long id) {

        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<CustomPaymentMethodResponseDto>> getAll() {

        return ResponseEntity.ok(service.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomPaymentMethodResponseDto> update(@PathVariable Long id, @Valid @RequestBody CustomPaymentMethodRequestDto request) {

        return ResponseEntity.ok(service.update(id, request));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activate(@PathVariable Long id) {

        service.activate(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> exists(@RequestParam String name, @RequestParam(required = false) Long excludeId) {

        return ResponseEntity.ok(service.existsByName(name, excludeId));
    }
}
