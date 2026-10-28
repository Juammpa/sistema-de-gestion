package org.sistemagestion.backend.repository;

import org.sistemagestion.backend.dto.stock.AvailableCutDto;
import org.sistemagestion.backend.model.Sale;
import org.sistemagestion.backend.model.StockCut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface StockCutRepository extends JpaRepository<StockCut, Long> {

    // Unidades de una pieza (para conservar las vendidas al reemplazar sus cortes)
    List<StockCut> findByPurchaseIdAndPieceNumber(Long purchaseId, Integer pieceNumber);

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

    // Unidades sin vender de cortes activos, de la mas antigua a la mas nueva (primero entra, primero sale [FIFO])
    @Query("""
            select new org.sistemagestion.backend.dto.stock.AvailableCutDto(
                c.id, cm.id, cm.cutMeatName, c.kilograms, p.id, c.pieceNumber, p.unit,
                p.date, pt.name, s.companyName)
            from StockCut c
            join c.cutMeat cm
            join c.purchase p
            join p.purchaseType pt
            join Supplier s on s.currentAccount = p.currentAccount
            where c.sale is null and cm.isActive = true
            order by p.date, p.id, c.pieceNumber, c.id
            """)
    List<AvailableCutDto> findAvailable();

    // Unidades de una venta, con su origen, de la más antigua a la más nueva
    @Query("""
            select new org.sistemagestion.backend.dto.stock.AvailableCutDto(
                c.id, cm.id, cm.cutMeatName, c.kilograms, p.id, c.pieceNumber, p.unit,
                p.date, pt.name, s.companyName)
            from StockCut c
            join c.cutMeat cm
            join c.purchase p
            join p.purchaseType pt
            join Supplier s on s.currentAccount = p.currentAccount
            where c.sale.id = :saleId
            order by p.date, p.id, c.pieceNumber, c.id
            """)
    List<AvailableCutDto> findDetailsBySaleId(@Param("saleId") Long saleId);

    // Reserva unidades para una venta. Solo toma las que siguen libres: si devuelve menos filas
    // que unidades pedidas, otra venta se las llevó antes.
    @Modifying(flushAutomatically = true)
    @Query("update StockCut c set c.sale = :sale where c.id in :ids and c.sale is null")
    int claim(@Param("sale") Sale sale, @Param("ids") Collection<Long> ids);

    // Libera las unidades de la venta que ya no están elegidas
    @Modifying(flushAutomatically = true)
    @Query("update StockCut c set c.sale = null where c.sale.id = :saleId and c.id not in :keepIds")
    void releaseExcept(@Param("saleId") Long saleId, @Param("keepIds") Collection<Long> keepIds);

    // Libera todas las unidades de la venta (al eliminarla)
    @Modifying(flushAutomatically = true)
    @Query("update StockCut c set c.sale = null where c.sale.id = :saleId")
    void releaseAll(@Param("saleId") Long saleId);

}
