package org.sistemagestion.backend.service.movement;

import org.sistemagestion.backend.dto.common.SliceResponseDto;
import org.sistemagestion.backend.dto.movement.MovementResponseDto;
import org.sistemagestion.backend.model.enums.MovementType;

public interface IMovementService {

    // type: null = todos los tipos de movimientos
    SliceResponseDto<MovementResponseDto> getMovementsByCurrentAccount(Long currentAccountId, MovementType type , int page, int size);
}
