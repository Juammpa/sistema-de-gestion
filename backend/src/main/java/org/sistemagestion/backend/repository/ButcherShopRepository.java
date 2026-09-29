package org.sistemagestion.backend.repository;

import org.sistemagestion.backend.model.ButcherShop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ButcherShopRepository extends JpaRepository<ButcherShop, Long> {

    List<ButcherShop> findByClientId(Long clientId);
    boolean existsByCurrentAccountId(Long currentAccountId);
    Optional<ButcherShop> findByCurrentAccountId(Long currentAccountId);

    // true si la cuenta corriente pertenece a la sucursal de un cliente eliminado
    boolean existsByCurrentAccountIdAndClientIsActiveFalse(Long currentAccountId);
}
