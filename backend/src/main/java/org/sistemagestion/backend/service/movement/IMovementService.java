package org.sistemagestion.backend.service.movement;

import org.sistemagestion.backend.dto.common.SliceResponseDto;
import org.sistemagestion.backend.dto.movement.MovementResponseDto;

public interface IMovementService {
    SliceResponseDto<MovementResponseDto> getMovementsByCurrentAccount(Long currentAccountId, int page, int size);
}
