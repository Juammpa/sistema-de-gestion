package org.sistemagestion.backend.repository;

import org.sistemagestion.backend.model.CutMeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CutMeatRepository extends JpaRepository<CutMeat, Long> {
}
