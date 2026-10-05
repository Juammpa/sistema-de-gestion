package org.sistemagestion.backend.service.stock;

import org.sistemagestion.backend.dto.stock.StockResponseDto;

import java.time.LocalDate;

public interface IStockService {

    // Piezas de stock y resumen de las compras del rango (por defecto, los ultimos 7 dias)
    StockResponseDto getStock(LocalDate from, LocalDate to);

}
