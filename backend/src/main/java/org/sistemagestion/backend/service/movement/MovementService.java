package org.sistemagestion.backend.service.movement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sistemagestion.backend.dto.movement.MovementResponseDto;
import org.sistemagestion.backend.exception.ResourceNotFoundException;
import org.sistemagestion.backend.mapper.MovementMapper;
import org.sistemagestion.backend.model.ButcherShop;
import org.sistemagestion.backend.repository.ButcherShopRepository;
import org.sistemagestion.backend.repository.MovementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovementService implements IMovementService{

    private final MovementRepository movementRepository;
    private final ButcherShopRepository butcherShopRepository;
    private final MovementMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<MovementResponseDto> getMovementsByCurrentAccount(Long currenAccountId) {
        return movementRepository.findByCurrentAccountIdOrderByDateAscIdAsc(currenAccountId).stream()
                .map(mapper::toDto)
                .toList();
    }
}
