package org.sistemagestion.backend.repository;

import org.sistemagestion.backend.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    List<Client> findByIsActiveTrue();
    Optional<Client> findByIdAndIsActiveTrue(Long id);
}
