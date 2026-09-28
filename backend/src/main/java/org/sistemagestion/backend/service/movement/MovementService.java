package org.sistemagestion.backend.service.movement;

import lombok.RequiredArgsConstructor;
import org.sistemagestion.backend.dto.common.SliceResponseDto;
import org.sistemagestion.backend.dto.movement.MovementResponseDto;
import org.sistemagestion.backend.mapper.MovementMapper;
import org.sistemagestion.backend.model.Movement;
import org.sistemagestion.backend.model.enums.MovementType;
import org.sistemagestion.backend.repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class MovementService implements IMovementService{

    private final MovementRepository movementRepository;
    private final SaleRepository saleRepository;
    private final PurchaseRepository purchaseRepository;
    private final PaymentRepository paymentRepository;
    private final ButcherShopRepository butcherShopRepository;
    private final MovementMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public SliceResponseDto<MovementResponseDto> getMovementsByCurrentAccount(Long currentAccountId, MovementType type, int page, int size) {

        Slice<? extends Movement> movements = findMovements(currentAccountId, type, PageRequest.of(page, size));

        return SliceResponseDto.from(movements, mapper::toDto);
    }

    // Sin tipo: todos los movimientos.
    // Con tipo: solo los de ese subtipo
    private Slice<? extends Movement> findMovements(Long currentAccountId, MovementType type, Pageable pageable) {
        if (type == null) {
            return movementRepository.findByCurrentAccountIdOrderByDateDescIdDesc(currentAccountId, pageable);
        }

        return switch (type) {
            case SALE -> saleRepository.findByCurrentAccountIdOrderByDateDescIdDesc(currentAccountId, pageable);
            case PURCHASE -> purchaseRepository.findByCurrentAccountIdOrderByDateDescIdDesc(currentAccountId, pageable);
            case PAYMENT -> paymentRepository.findByCurrentAccountIdOrderByDateDescIdDesc(currentAccountId, pageable);
        };
    }
}
