package org.sistemagestion.backend.repository;

import org.sistemagestion.backend.model.Payment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOriginPaymentId(Long originPaymentId);

    // Pagos de una cuenta corriente (paginado, del mas reciente al mas antiguo)
    Slice<Payment> findByCurrentAccountIdOrderByDateDescIdDesc(Long currentAccountId, Pageable pageable);

}
