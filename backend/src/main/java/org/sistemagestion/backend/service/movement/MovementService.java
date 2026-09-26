package org.sistemagestion.backend.service.movement;

import lombok.RequiredArgsConstructor;
import org.sistemagestion.backend.dto.common.SliceResponseDto;
import org.sistemagestion.backend.dto.movement.MovementResponseDto;
import org.sistemagestion.backend.mapper.MovementMapper;
import org.sistemagestion.backend.repository.MovementRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class MovementService implements IMovementService{

    private final MovementRepository movementRepository;
    private final MovementMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public SliceResponseDto<MovementResponseDto> getMovementsByCurrentAccount(Long currentAccountId, int page, int size) {
        return SliceResponseDto.from(
                movementRepository.findByCurrentAccountIdOrderByDateDescIdDesc(
                        currentAccountId,
                        PageRequest.of(page, size)),
                mapper::toDto);
    }
}
