package org.sistemagestion.backend.service.movement;

import lombok.RequiredArgsConstructor;
import org.sistemagestion.backend.common.Decimals;
import org.sistemagestion.backend.model.CurrentAccount;
import org.sistemagestion.backend.model.Movement;
import org.sistemagestion.backend.model.Payment;
import org.sistemagestion.backend.repository.MovementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BalanceRecalculationService {

    private final MovementRepository movementRepository;

    @Transactional
    public void recalculate(CurrentAccount currentAccount) {

        List<Movement> movements = movementRepository
                .findByCurrentAccountIdOrderByDateAscIdAsc(currentAccount.getId());

        BigDecimal runningBalance = Decimals.MONEY_ZERO;
        for (Movement movement : movements) {
            BigDecimal amount = Decimals.moneyOrZero(movement.getAmount());
            BigDecimal signedAmount = movement instanceof Payment ? amount.negate() : amount;

            runningBalance = runningBalance.add(signedAmount);
            movement.setBalance(runningBalance);
        }

        currentAccount.setCurrentBalance(runningBalance);
    }

}
