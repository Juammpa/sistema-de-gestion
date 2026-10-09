package org.sistemagestion.backend.repository;

import org.sistemagestion.backend.model.CutMeat;
import org.sistemagestion.backend.model.enums.Specie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CutMeatRepository extends JpaRepository<CutMeat, Long> {

    // Nombre ya usado en la misma especie, por un corte activo o eliminado
    boolean existsByCutMeatNameIgnoreCaseAndSpecie(String cutMeatName, Specie specie);
    // Igual, pero ignorando el corte que se esta editando
    boolean existsByCutMeatNameIgnoreCaseAndSpecieAndIdNot(String cutMeatName, Specie specie, Long id);



}
