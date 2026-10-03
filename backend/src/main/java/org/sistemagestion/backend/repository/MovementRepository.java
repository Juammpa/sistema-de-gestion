package org.sistemagestion.backend.repository;

import org.sistemagestion.backend.model.Movement;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Repository
public interface MovementRepository extends JpaRepository<Movement, Long> {

    // Para listar (paginado, del mas reciente al mas antiguo, dentro de un rango de fechas)
    Slice<Movement> findByCurrentAccountIdAndDateBetweenOrderByDateDescIdDesc(Long currentAccountId, LocalDate from, LocalDate to, Pageable pageable);

    // Para recalcular
    List<Movement> findByCurrentAccountIdOrderByDateAscIdAsc(Long currentAccountId);

    // Ultimos movimientos de todas las cuentas (sin los pagos espejos, que duplican el cobro del cliente)
    @Query("""
            select m from Movement m
            where m.id not in (select p.id from Payment p where p.originPayment is not null)
            order by m.date desc, m.id desc
            """)
    List<Movement> findRecent(Pageable pageable);

    // Para exportar: movimientos de varias cuentas juntas, del mas viejo al mas nuevo
    List<Movement> findByCurrentAccountIdInOrderByDateAscIdAsc(Collection<Long> currentAccountIds);
}

