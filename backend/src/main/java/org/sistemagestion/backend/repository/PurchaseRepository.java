package org.sistemagestion.backend.repository;

import org.sistemagestion.backend.model.Purchase;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    // Compras de una cuenta corriente (paginado, de la mas reciente a la mas antigua, dentro de un rango)
    Slice<Purchase> findByCurrentAccountIdAndDateBetweenOrderByDateDescIdDesc(Long currentAccountId, LocalDate from, LocalDate to, Pageable pageable);


}
