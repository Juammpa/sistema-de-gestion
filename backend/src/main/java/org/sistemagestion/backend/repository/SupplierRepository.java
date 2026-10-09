package org.sistemagestion.backend.repository;

import org.sistemagestion.backend.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    boolean existsByCurrentAccountId(Long currentAccounId);
    Optional<Supplier> findByCurrentAccountId(Long currentAccountId);

    List<Supplier> findByIsActiveTrue();
    Optional<Supplier> findByIdAndIsActiveTrue(Long id);
    boolean existsByCurrentAccountIdAndIsActiveFalse(Long currentAccountId);

    // Para exportar: proveedores activos ordenados por nombre
    List<Supplier> findByIsActiveTrueOrderByCompanyNameAsc();

    // Nombre ya usado por un proveedor activo (sin distinguir mayusculas)
    boolean existsByCompanyNameIgnoreCaseAndIsActiveTrue(String companyName);
    // Igual, pero ignorando al proveedor que se esta editando
    boolean existsByCompanyNameIgnoreCaseAndIsActiveTrueAndIdNot(String companyName, Long id);


}
