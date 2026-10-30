package org.sistemagestion.backend.repository;

import org.sistemagestion.backend.model.StockPieceClosure;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface StockPieceClosureRepository extends JpaRepository<StockPieceClosure, Long> {

    // Cierres de varias compras de una sola vez (para armar la lista de stock)
    List<StockPieceClosure> findByPurchaseIdIn(Collection<Long> purchaseIds);

    boolean existsByPurchaseIdAndPieceNumber(Long purchaseId, Integer pieceNumber);

    void deleteByPurchaseIdAndPieceNumber(Long purchaseId, Integer pieceNumber);

    // Al borrar la compra o cambiar su reparto, los cierres dejan de tener sentido
    void deleteByPurchaseId(Long purchaseId);
}
