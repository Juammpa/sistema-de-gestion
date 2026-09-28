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

import java.time.LocalDate;


@Service
@RequiredArgsConstructor
public class MovementService implements IMovementService{

    // Limites que se usan cuando no se indica "desde" o "hasta"
    private static final LocalDate MIN_DATE = LocalDate.of(1900,1,1);
    private static final LocalDate MAX_DATE = LocalDate.of(9999,12,31);

    private final MovementRepository movementRepository;
    private final SaleRepository saleRepository;
    private final PurchaseRepository purchaseRepository;
    private final PaymentRepository paymentRepository;
    private final ButcherShopRepository butcherShopRepository;
    private final MovementMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public SliceResponseDto<MovementResponseDto> getMovementsByCurrentAccount(Long currentAccountId, MovementType type, LocalDate from, LocalDate to, int page, int size) {

        LocalDate start = from != null ? from : MIN_DATE;
        LocalDate end = to != null ? to : MAX_DATE;

        Slice<? extends Movement> movements =
                findMovements(currentAccountId, type, start, end, PageRequest.of(page, size));

        return SliceResponseDto.from(movements, mapper::toDto);
    }

    // Sin tipo: todos los movimientos.
    // Con tipo: solo los de ese subtipo
    private Slice<? extends Movement> findMovements(Long currentAccountId, MovementType type, LocalDate from, LocalDate to, Pageable pageable) {
        if (type == null) {
            return movementRepository.findByCurrentAccountIdAndDateBetweenOrderByDateDescIdDesc(currentAccountId, from, to, pageable);
        }

        return switch (type) {
            case SALE -> saleRepository.findByCurrentAccountIdAndDateBetweenOrderByDateDescIdDesc(currentAccountId, from, to, pageable);
            case PURCHASE -> purchaseRepository.findByCurrentAccountIdAndDateBetweenOrderByDateDescIdDesc(currentAccountId, from, to,pageable);
            case PAYMENT -> paymentRepository.findByCurrentAccountIdAndDateBetweenOrderByDateDescIdDesc(currentAccountId, from, to, pageable);
        };
    }
}
