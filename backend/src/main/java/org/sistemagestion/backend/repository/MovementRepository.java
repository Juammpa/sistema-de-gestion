package org.sistemagestion.backend.repository;

import org.sistemagestion.backend.model.Movement;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MovementRepository extends JpaRepository<Movement, Long> {

    // Para listar (paginado, del mas reciente al mas antiguo, dentro de un rango de fechas)
    Slice<Movement> findByCurrentAccountIdAndDateBetweenOrderByDateDescIdDesc(Long currentAccountId, LocalDate from, LocalDate to, Pageable pageable);

    // Para recalcular
    List<Movement> findByCurrentAccountIdOrderByDateAscIdAsc(Long currentAccountId);
}

