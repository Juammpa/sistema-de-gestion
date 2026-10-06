package org.sistemagestion.backend.service.stock;

import lombok.RequiredArgsConstructor;
import org.sistemagestion.backend.dto.stock.StockItemDto;
import org.sistemagestion.backend.dto.stock.StockPurchaseDto;
import org.sistemagestion.backend.dto.stock.StockResponseDto;
import org.sistemagestion.backend.dto.stock.StockSummaryDto;
import org.sistemagestion.backend.exception.BusinessRuleException;
import org.sistemagestion.backend.exception.ResourceNotFoundException;
import org.sistemagestion.backend.model.Purchase;
import org.sistemagestion.backend.model.PurchasePieceWeight;
import org.sistemagestion.backend.model.enums.PurchaseType;
import org.sistemagestion.backend.model.enums.Specie;
import org.sistemagestion.backend.repository.PurchasePieceWeightRepository;
import org.sistemagestion.backend.repository.PurchaseRepository;
import org.sistemagestion.backend.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockService implements IStockService {

    private static final int DEFAULT_DAYS = 7;

    private final StockRepository stockRepository;
    private final PurchasePieceWeightRepository pieceWeightRepository;
    private final PurchaseRepository purchaseRepository;

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

    @Override
    @Transactional
    public void updatePieceWeights(Long purchaseId, List<Double> kilograms) {

        Purchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase", "id", purchaseId));

        int units = purchase.getUnit() != null && purchase.getUnit() > 0 ? purchase.getUnit() : 1;

        if (units < 2) {
            throw new BusinessRuleException(
                    "Solo se puede repartir el peso de compras con más de una unidad."
            );
        }

        if (kilograms.size() != units) {
            throw new BusinessRuleException(String.format(
                    "La compra tiene %d unidades: ingresá el peso de cada una.", units));
        }

        List<BigDecimal> weights = kilograms.stream().map(this::toTwoDecimal).toList();

        // Un peso que redondea a 0 (por ejemplo, 0,004) tampoco es valido
        if (weights.stream().anyMatch(weight -> weight.signum() <= 0)) {
            throw new BusinessRuleException("Cada pieza debe pesar más de 0 kg.");
        }

        BigDecimal total = toTwoDecimal(purchase.getKilograms() != null ? purchase.getKilograms() : 0.0);
        BigDecimal sum = weights.stream().reduce(BigDecimal.ZERO, BigDecimal::add);

        if (sum.compareTo(total) != 0) {
            throw new BusinessRuleException(String.format(
                    "La suma de las piezas (%s kg) debe ser igual a los kilos de la compra (%s kg).",
                    formatKilograms(sum), formatKilograms(total)));
        }

        // Se reemplaza el reparto completo: primero se borra el anterior (si lo habia)
        pieceWeightRepository.deleteByPurchaseId(purchaseId);
        List<Double> values = weights.stream().map(BigDecimal::doubleValue).toList();

        // Si el usuario dejo el reparto parejo, no hay nada que guardar: no queda como "ajustada".
        if (values.equals(PieceWeights.evenSplit(total.doubleValue(),units))) {
            return;
        }

        List<PurchasePieceWeight> rows = new ArrayList<>();
        for (int index = 0; index < units; index++) {
            rows.add(PurchasePieceWeight.builder()
                    .purchase(purchase)
                    .pieceNumber(index + 1)
                    .kilograms(values.get(index))
                    .build());
        }

        pieceWeightRepository.saveAll(rows);
    }

    // Cada compra se parte en tantas piezas como unidades tenga. El peso sale del ajuste
    // guardado (si lo hay) o de repartir los kilos en partes iguales.
    // Mantiene el orden de la consulta: compra mas reciente primero
    private List<StockItemDto> splitIntoItems(List<StockPurchaseDto> purchases) {

        Map<Long, List<PurchasePieceWeight>> savedByPurchase = loadSavedWeights(purchases);
        List<StockItemDto> items = new ArrayList<>();

        for (StockPurchaseDto purchase: purchases) {
            int units = unitsOf(purchase);
            PieceWeights.Distribution distribution = PieceWeights.resolve(
                    units, kilogramsOf(purchase), savedByPurchase.get(purchase.id())
            );

            for (int number = 1; number <= units; number++) {
                items.add(new StockItemDto(
                        purchase.id(),
                        number,
                        units,
                        purchase.purchaseType(),
                        purchase.purchaseType().getSpecie(),
                        distribution.kilograms().get(number - 1),
                        purchase.date(),
                        purchase.supplierId(),
                        purchase.supplierName(),
                        units > 1,
                        distribution.adjusted()));
            }
        }

        return items;
    }

    // Pesos ajustados de todas las compras del rango, con una sola consulta
    private Map<Long, List<PurchasePieceWeight>> loadSavedWeights(List<StockPurchaseDto> purchases) {

        if (purchases.isEmpty()) {
            return Map.of();
        }

        List<Long> purchaseIds = purchases.stream().map(StockPurchaseDto::id).toList();

        return pieceWeightRepository.findByPurchaseIdIn(purchaseIds).stream()
                .collect(Collectors.groupingBy(weight -> weight.getPurchase().getId()));
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

    private BigDecimal toTwoDecimal(double kilograms) {
        return BigDecimal.valueOf(kilograms).setScale(2, RoundingMode.HALF_UP);
    }

    // Coma decimal, como lo ve el usuario (210,00)
    private String formatKilograms(BigDecimal kilograms) {
        return String.format(Locale.forLanguageTag("es-AR"), "%.2f", kilograms);
    }
}

