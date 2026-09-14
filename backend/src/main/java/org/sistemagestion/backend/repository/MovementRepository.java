package org.sistemagestion.backend.repository;

import org.sistemagestion.backend.model.Movement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovementRepository extends JpaRepository<Movement, Long> {

    List<Movement> findByCurrentAccountIdOrderByDateDescIdDesc(Long currentAccountId);
}
