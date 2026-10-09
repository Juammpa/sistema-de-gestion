package org.sistemagestion.backend.repository;

import org.sistemagestion.backend.model.ButcherShop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ButcherShopRepository extends JpaRepository<ButcherShop, Long> {

    boolean existsByCurrentAccountId(Long currentAccountId);
    Optional<ButcherShop> findByCurrentAccountId(Long currentAccountId);

    // true si la cuenta corriente pertenece a la sucursal de un cliente eliminado
    boolean existsByCurrentAccountIdAndClientIsActiveFalse(Long currentAccountId);

    // Solo sucursales no eliminadas
    Optional<ButcherShop> findByIdAndIsActiveTrue(Long id);

    // Cuantas sucursales activas tiene un cliente (debe tener al menos 1)
    long countByClientIdAndIsActiveTrue(Long clientId);

    // true si la cuenta corriente pertenece a una sucursal eliminada
    boolean existsByCurrentAccountIdAndIsActiveFalse(Long currentAccountId);

    // Nombre ya usado por otra sucursal activa del mismo cliente
    boolean existsByClientIdAndNameIgnoreCaseAndIsActiveTrue(Long clientId, String name);
    // Igual, pero ignorando la sucursal que se esta editando
    boolean existsByClientIdAndNameIgnoreCaseAndIsActiveTrueAndIdNot(Long clientId, String name, Long id);

}
