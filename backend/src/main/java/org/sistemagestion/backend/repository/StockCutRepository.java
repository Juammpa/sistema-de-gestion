package org.sistemagestion.backend.repository;

import org.sistemagestion.backend.model.StockCut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface StockCutRepository extends JpaRepository<StockCut, Long> {

    // Borra todos los cortes de una pieza (se reemplazan por los nuevos)
    @Modifying
    @Query("delete from StockCut c where c.purchase.id = :purchaseId and c.pieceNumber = :pieceNumber")
    void deleteByPurchaseIdAndPieceNumber(@Param("purchaseId") Long purchaseId, @Param("pieceNumber") Integer pieceNumber);

    // Cortes de varias compras de una sola vez (para armar la lista de stock), con el nombre del corte
    @Query("""
        select c from StockCut c
        join fetch c.cutMeat
        where c.purchase.id in :purchaseIds
        order by c.id 
            """)
    List<StockCut> findByPurchaseIdIn(@Param("purchaseIds")Collection<Long> purchaseIds);

    // Todos los cortes de una compra (para revalidar al cambiar el reparto de pesos)
    List<StockCut> findByPurchaseId(Long purchaseId);

    // Verifica si la compra ya tiene cortes cargados en alguna de sus piezas
    boolean existsByPurchaseId(Long purchaseId);


}
