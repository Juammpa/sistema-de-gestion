package org.sistemagestion.backend.repository;

import org.sistemagestion.backend.model.PurchaseType;
import org.sistemagestion.backend.model.enums.Specie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseTypeRepository extends JpaRepository<PurchaseType, Long> {

    // Todos los tipos (activos y archivados), ordenados para mostrarlos
    List<PurchaseType> findAllByOrderBySpecieAscNameAsc();
    List<PurchaseType> findAllBySpecieOrderByNameAsc(Specie specie);

    // Nombre ya usado por un tipo ACTIVO de la misma especie
    boolean existsByNameIgnoreCaseAndSpecieAndIsActiveTrue(String name, Specie specie);
    // Igual, pero ignorando el tipo que se esta editando
    boolean existsByNameIgnoreCaseAndSpecieAndIsActiveTrueAndIdNot(String name, Specie specie, Long id);
}
