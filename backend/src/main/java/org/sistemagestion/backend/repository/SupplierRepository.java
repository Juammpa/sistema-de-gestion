package org.sistemagestion.backend.repository;

import org.sistemagestion.backend.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    boolean existsByCurrentAccountId(Long currentAccounId);
    Optional<Supplier> findByCurrentAccountId(Long currentAccountId);
}
