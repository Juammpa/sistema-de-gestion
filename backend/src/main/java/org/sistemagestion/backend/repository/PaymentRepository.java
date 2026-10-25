package org.sistemagestion.backend.repository;

import org.sistemagestion.backend.model.Payment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOriginPaymentId(Long originPaymentId);

    // Pagos de una cuenta corriente (paginado, del mas reciente al mas antiguo, dentro de un rango)
    Slice<Payment> findByCurrentAccountIdAndDateBetweenOrderByDateDescIdDesc(Long currentAccountId, LocalDate from, LocalDate to, Pageable pageable);

    // Indica si el cheque ya tiene un rechazo registrado
    boolean existsByRejectedPaymentId(Long rejectedPaymentId);
}
