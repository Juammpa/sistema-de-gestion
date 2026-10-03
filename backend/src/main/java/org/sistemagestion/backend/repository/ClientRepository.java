package org.sistemagestion.backend.repository;

import org.sistemagestion.backend.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    List<Client> findByIsActiveTrue();
    Optional<Client> findByIdAndIsActiveTrue(Long id);

    @Query("""
    select c from Client c
    left join fetch c.butcherShops bs
    left join fetch bs.currentAccount
    where c.isActive = true
    order by c.lastName, c.name, bs.id
""")
    List<Client> findActiveWithButcherShops();
}
