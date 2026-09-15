package org.sistemagestion.backend.service.movement;

import lombok.RequiredArgsConstructor;
import org.sistemagestion.backend.model.CurrentAccount;
import org.sistemagestion.backend.model.Movement;
import org.sistemagestion.backend.model.Payment;
import org.sistemagestion.backend.repository.MovementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BalanceRecalculationService {

    private final MovementRepository movementRepository;

    @Transactional
    public void recalculate(CurrentAccount currentAccount) {

        List<Movement> movements = movementRepository
                .findByCurrentAccountIdOrderByDateAscIdAsc(currentAccount.getId());

        Double runningBalance = 0.0;
        for (Movement movement : movements) {
            Double signedAmount = movement instanceof Payment ? -
                    movement.getAmount() : movement.getAmount();

            runningBalance += signedAmount;
            movement.setBalance(runningBalance);
        }

        currentAccount.setCurrentBalance(runningBalance);
    }

}
