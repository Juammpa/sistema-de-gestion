package org.sistemagestion.backend.service.account;

import lombok.RequiredArgsConstructor;
import org.sistemagestion.backend.exception.BusinessRuleException;
import org.sistemagestion.backend.model.CurrentAccount;
import org.sistemagestion.backend.repository.ButcherShopRepository;
import org.sistemagestion.backend.repository.SupplierRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountGuard {

    private final SupplierRepository supplierRepository;
    private final ButcherShopRepository butcherShopRepository;

    // Rechaza operar sobre la cuenta de un cliente o proveedor eliminado
    public void assertOwnerActive(CurrentAccount account) {
        Long accountId = account.getId();

        if (supplierRepository.existsByCurrentAccountIdAndIsActiveFalse(accountId) ||
                butcherShopRepository.existsByCurrentAccountIdAndClientIsActiveFalse(accountId)
            || butcherShopRepository.existsByCurrentAccountIdAndIsActiveFalse(accountId)) {

            throw new BusinessRuleException(
                    "No se pueden registrar movimientos: el cliente, la sucursal o el proveedor fue eliminado."
            );
        }
    }

}
