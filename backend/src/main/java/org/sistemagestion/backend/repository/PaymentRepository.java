package org.sistemagestion.backend.repository;

import org.sistemagestion.backend.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOriginPaymentId(Long originPaymentId);
}
