package org.sistemagestion.backend.controller;

import lombok.RequiredArgsConstructor;
import org.sistemagestion.backend.dto.common.SliceResponseDto;
import org.sistemagestion.backend.dto.movement.MovementResponseDto;
import org.sistemagestion.backend.model.enums.MovementType;
import org.sistemagestion.backend.service.movement.IMovementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/movements")
@RequiredArgsConstructor
public class MovementController {

    private static final int MAX_PAGE_SIZE = 100;
    private final IMovementService movementService;

    @GetMapping
    public ResponseEntity<SliceResponseDto<MovementResponseDto>> getMovementsByCurrentAccount(
            @RequestParam Long currentAccountId,
            @RequestParam(required = false)MovementType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);

        return ResponseEntity.ok(movementService.getMovementsByCurrentAccount(currentAccountId, type, safePage,safeSize));
    }


}
