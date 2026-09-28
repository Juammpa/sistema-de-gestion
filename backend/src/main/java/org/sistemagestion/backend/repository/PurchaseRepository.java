package org.sistemagestion.backend.repository;

import org.sistemagestion.backend.model.Purchase;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    // Compras de una cuenta corriente (paginado, de la mas reciente a la mas antigua)
    Slice<Purchase> findByCurrentAccountIdOrderByDateDescIdDesc(Long currentAccountId, Pageable pageable);


}
