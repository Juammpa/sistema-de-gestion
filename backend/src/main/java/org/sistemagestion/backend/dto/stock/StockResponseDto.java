package org.sistemagestion.backend.dto.stock;

import java.util.List;

public record StockResponseDto(
        StockSummaryDto summary,
        List<StockItemDto> items
) {}
