package org.sistemagestion.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sistemagestion.backend.dto.cut_meat.CutMeatRequestDto;
import org.sistemagestion.backend.dto.cut_meat.CutMeatResponseDto;
import org.sistemagestion.backend.model.enums.Specie;
import org.sistemagestion.backend.service.cut_meat.ICutMeatService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/cut-meat")
@RequiredArgsConstructor
public class CutMeatController {

    private final ICutMeatService cutMeatService;

    @PostMapping
    public ResponseEntity<CutMeatResponseDto> createCutMeat(@Valid @RequestBody CutMeatRequestDto request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(cutMeatService.createCutMeat(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CutMeatResponseDto> getById(@PathVariable Long id) {

        return ResponseEntity.ok(cutMeatService.getCutMeatById(id));
    }

    @GetMapping
    public ResponseEntity<List<CutMeatResponseDto>> getAllCutMeats() {

        return ResponseEntity.ok(cutMeatService.getAllCutMeats());
    }

    @PutMapping("/{id}")
    public ResponseEntity<CutMeatResponseDto> updateCutMeat(@PathVariable Long id, @Valid @RequestBody CutMeatRequestDto request) {

        return ResponseEntity.ok(cutMeatService.updateCutMeat(id, request));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activateCutMeatById(@PathVariable Long id) {

        cutMeatService.activeCutMeatById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {

        cutMeatService.deleteCutMeatById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> existsByNameAndSpecie(@RequestParam String name, @RequestParam Specie specie, @RequestParam(required = false) Long excludeId) {

        return ResponseEntity.ok(cutMeatService.existsByNameAndSpecie(name, specie, excludeId));
    }

}
