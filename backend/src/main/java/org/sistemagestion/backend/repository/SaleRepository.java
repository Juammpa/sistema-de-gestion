package org.sistemagestion.backend.repository;

import org.sistemagestion.backend.model.Sale;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {

    List<Sale> findByClientId(Long clientId);

    // Ventas de una cuenta corriente (paginado, de la mas reciente a la mas antigua)
    Slice<Sale> findByCurrentAccountIdOrderByDateDescIdDesc(Long currentAccountId, Pageable pageable);
}
