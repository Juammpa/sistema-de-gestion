package org.sistemagestion.backend.service.movement;

import org.sistemagestion.backend.dto.movement.MovementResponseDto;

import java.util.List;

public interface IMovementService {
    List<MovementResponseDto> getMovementsByCurrentAccount(Long currenAccountId);
}
