package org.sistemagestion.backend.repository;

import org.sistemagestion.backend.model.ButcherShop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ButcherShopRepository extends JpaRepository<ButcherShop, Long> {

    List<ButcherShop> findByClientId(Long clientId);

}
