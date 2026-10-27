package org.sistemagestion.backend.service.stock;

import org.sistemagestion.backend.dto.stock.StockCutsRequestDto;
import org.sistemagestion.backend.dto.stock.StockResponseDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface IStockService {

    // Piezas de stock y resumen de las compras del rango (por defecto, los ultimos 7 dias)
    StockResponseDto getStock(LocalDate from, LocalDate to);

    // Reparte los kilos de una compra entre sus piezas (la suma debe dar igual a la compra)
    void updatePieceWeights(Long purchaseId, List<BigDecimal> kilograms);

    // Reemplaza los cortes de una pieza (la suma no puede superar el peso de la pieza)
    void updatePieceCuts(Long purchaseId, int pieceNumber, List<StockCutsRequestDto.Cut> cuts);
}
