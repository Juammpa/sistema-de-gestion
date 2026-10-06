package org.sistemagestion.backend.repository;

import org.sistemagestion.backend.model.PurchasePieceWeight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface PurchasePieceWeightRepository extends JpaRepository<PurchasePieceWeight, Long> {

    // Pesos ajustados de varias compras de una sola vez (para armar la lista de stock)
    List<PurchasePieceWeight> findByPurchaseIdIn(Collection<Long> purchaseIds);

    // Pesos ajustados de una compra, en orden de pieza
    List<PurchasePieceWeight> findByPurchaseIdOrderByPieceNumber(Long purchaseId);

    // Vuelve al reparto parejo: borra todos los ajustes de la compra
    @Modifying
    @Query("delete from PurchasePieceWeight w where w.purchase.id = :purchaseId")
    void deleteByPurchaseId(@Param("purchaseId") Long purchaseId);
}
