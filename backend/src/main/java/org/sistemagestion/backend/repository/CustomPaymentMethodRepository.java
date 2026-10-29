package org.sistemagestion.backend.repository;

import org.sistemagestion.backend.model.CustomPaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomPaymentMethodRepository extends JpaRepository<CustomPaymentMethod, Long> {

    // Todos los metodos (activos y archivados), ordenados para mostrarlos
    List<CustomPaymentMethod> findAllByOrderByNameAsc();

    // Nombre ya usado por un metodo ACTIVO
    boolean existsByNameIgnoreCaseAndIsActiveTrue(String name);
    // Igual, pero ignorando el metodo que se esta editando
    boolean existsByNameIgnoreCaseAndIsActiveTrueAndIdNot(String name, Long id);

    // Solo un metodo activo (para elegirlo al cargar un pago)
    Optional<CustomPaymentMethod> findByIdAndIsActiveTrue(Long id);

}
