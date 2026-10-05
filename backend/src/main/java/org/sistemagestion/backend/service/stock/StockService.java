package org.sistemagestion.backend.service.stock;

import lombok.RequiredArgsConstructor;
import org.sistemagestion.backend.dto.stock.StockItemDto;
import org.sistemagestion.backend.dto.stock.StockPurchaseDto;
import org.sistemagestion.backend.dto.stock.StockResponseDto;
import org.sistemagestion.backend.dto.stock.StockSummaryDto;
import org.sistemagestion.backend.exception.BusinessRuleException;
import org.sistemagestion.backend.model.enums.PurchaseType;
import org.sistemagestion.backend.model.enums.Specie;
import org.sistemagestion.backend.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockService implements IStockService {

    private static final int DEFAULT_DAYS = 7;

    private final StockRepository stockRepository;

    @Override
    @Transactional(readOnly = true)
    public StockResponseDto getStock(LocalDate from, LocalDate to) {

        // Filtros de fechas (desde / hasta)
        LocalDate end = to != null ? to : LocalDate.now();
        LocalDate start = from != null ? from : end.minusDays(DEFAULT_DAYS -1);

        if (start.isAfter(end)) {
            throw new BusinessRuleException(
                    "La fecha «desde» no puede ser posterior a la fecha «hasta»");
        }

        List<StockPurchaseDto> purchases = stockRepository.findPurchases(start, end);

        return new StockResponseDto(summarize(purchases), splitIntoItems(purchases));
    }

    // Cada compra se parte en tantas piezas como unidades tenga, todas con el mismo peso
    // (kilos / unidades). Mantiene el orden de la consulta: compra mas reciente primero
    private List<StockItemDto> splitIntoItems(List<StockPurchaseDto> purchases) {

        List<StockItemDto> items = new ArrayList<>();

        for (StockPurchaseDto purchase: purchases) {
            int units = unitsOf(purchase);
            double kilogramsPerUnit = kilogramsOf(purchase) / units;

            for (int number =1; number <= units; number++) {
                items.add(new StockItemDto(
                        purchase.id(),
                        number,
                        units,
                        purchase.purchaseType(),
                        purchase.purchaseType().getSpecie(),
                        kilogramsPerUnit,
                        purchase.date(),
                        purchase.supplierId(),
                        purchase.supplierName()));
            }
        }

        return items;
    }

    // Los totales salen de las compras (no de sumas las piezas), asi no se acumula error de redondeo
    private StockSummaryDto summarize(List<StockPurchaseDto> purchases) {

        // Un renglon por tipo de compra que tenga datos, en el orden del enum
        List<StockSummaryDto.TypeTotals> byType = new ArrayList<>();

        for (PurchaseType type: PurchaseType.values()) {
            List<StockPurchaseDto> ofType = purchases.stream()
                    .filter(purchase -> purchase.purchaseType() == type)
                    .toList();

            if (ofType.isEmpty()) {
                continue;
            }

            byType.add(new StockSummaryDto.TypeTotals(
                    type,
                    ofType.stream().mapToDouble(this::kilogramsOf).sum(),
                    ofType.stream().mapToInt(this::unitsOf).sum()
            ));
        }

        StockSummaryDto.SpecieTotals bovine = totalsOf(byType, Specie.BOVINE);
        StockSummaryDto.SpecieTotals porcine = totalsOf(byType, Specie.PORCINE);

        return new StockSummaryDto(
                bovine.kilograms() + porcine.kilograms(),
                bovine.units() + porcine.units(),
                bovine,
                porcine,
                byType);
    }

    private StockSummaryDto.SpecieTotals totalsOf(List<StockSummaryDto.TypeTotals> byType, Specie specie) {
        double kilograms = 0;
        int units = 0;

        for (StockSummaryDto.TypeTotals totals: byType) {
            if (totals.purchaseType().getSpecie() == specie) {
                kilograms += totals.kilograms();
                units += totals.units();
            }
        }

        return new StockSummaryDto.SpecieTotals(kilograms, units);
    }

    // Las compras nuevas exigen unidades y kilos positivos; esto solo cubre datos viejos incompletos
    private int unitsOf(StockPurchaseDto purchase) {
        return purchase.unit() != null && purchase.unit() > 0
                ? purchase.unit() : 1;
    }

    private double kilogramsOf(StockPurchaseDto purchase) {
        return purchase.kilograms() != null
                ? purchase.kilograms() : 0.0;
    }


}

