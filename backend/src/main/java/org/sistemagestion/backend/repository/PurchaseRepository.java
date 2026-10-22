package org.sistemagestion.backend.repository;

import org.sistemagestion.backend.model.Purchase;
import org.sistemagestion.backend.model.enums.Specie;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    // Compras de una cuenta corriente (paginado, de la mas reciente a la mas antigua, dentro de un rango)
    Slice<Purchase> findByCurrentAccountIdAndDateBetweenOrderByDateDescIdDesc(Long currentAccountId, LocalDate from, LocalDate to, Pageable pageable);

    // Verifica si cuenta tiene compras de alguna de las especies indicadas
    boolean existsByCurrentAccountIdAndPurchaseType_SpecieIn(Long currentAccountId, Collection<Specie> species);

}
