package org.sistemagestion.backend.service.stock;

import org.sistemagestion.backend.dto.stock.StockResponseDto;

import java.time.LocalDate;
import java.util.List;

public interface IStockService {

    // Piezas de stock y resumen de las compras del rango (por defecto, los ultimos 7 dias)
    StockResponseDto getStock(LocalDate from, LocalDate to);

    // Reparte los kilos de una compra entre sus piezas (la suma debe dar igual a la compra)
    void updatePieceWeights(Long purchaseId, List<Double> kilograms);
}
