package org.sistemagestion.backend.service.movement;

import org.sistemagestion.backend.dto.common.SliceResponseDto;
import org.sistemagestion.backend.dto.movement.MovementResponseDto;
import org.sistemagestion.backend.model.enums.MovementType;

import java.time.LocalDate;

public interface IMovementService {

    // type: null = todos los tipos de movimientos.
    // from / to: null = sin limite de ese lado.
    SliceResponseDto<MovementResponseDto> getMovementsByCurrentAccount(Long currentAccountId, MovementType type , LocalDate from,LocalDate to, int page, int size);
}
