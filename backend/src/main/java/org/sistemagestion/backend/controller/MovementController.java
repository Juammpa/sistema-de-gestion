package org.sistemagestion.backend.controller;

import lombok.RequiredArgsConstructor;
import org.sistemagestion.backend.dto.movement.MovementResponseDto;
import org.sistemagestion.backend.service.movement.IMovementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/movements")
@RequiredArgsConstructor
public class MovementController {

    private final IMovementService movementService;

    @GetMapping
    public ResponseEntity<List<MovementResponseDto>> getMovementsByCurrentAccount(@RequestParam Long currentAccountId) {

        return ResponseEntity.ok(movementService.getMovementsByCurrentAccount(currentAccountId));
    }


}
