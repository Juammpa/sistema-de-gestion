package org.sistemagestion.backend.service.stock;

import lombok.RequiredArgsConstructor;
import org.sistemagestion.backend.common.Decimals;
import org.sistemagestion.backend.dto.stock.*;
import org.sistemagestion.backend.exception.BusinessRuleException;
import org.sistemagestion.backend.exception.ResourceNotFoundException;
import org.sistemagestion.backend.model.CutMeat;
import org.sistemagestion.backend.model.Purchase;
import org.sistemagestion.backend.model.PurchasePieceWeight;
import org.sistemagestion.backend.model.StockCut;
import org.sistemagestion.backend.model.enums.Specie;
import org.sistemagestion.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockService implements IStockService {

    private static final int DEFAULT_DAYS = 7;

    private final StockRepository stockRepository;
    private final PurchasePieceWeightRepository pieceWeightRepository;
    private final PurchaseRepository purchaseRepository;
    private final CutMeatRepository cutMeatRepository;
    private final StockCutRepository stockCutRepository;

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
    public void updatePieceWeights(Long purchaseId, List<BigDecimal> kilograms) {

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

        List<BigDecimal> weights = kilograms.stream().map(Decimals::pieceKg).toList();

        // Un peso que redondea a 0 (por ejemplo, 0,004) tampoco es valido
        if (weights.stream().anyMatch(weight -> weight.signum() <= 0)) {
            throw new BusinessRuleException("Cada pieza debe pesar más de 0 kg.");
        }

        BigDecimal total = Decimals.pieceKg(purchase.getKilograms());
        BigDecimal sum = weights.stream().reduce(BigDecimal.ZERO, BigDecimal::add);

        if (sum.compareTo(total) != 0) {
            throw new BusinessRuleException(String.format(
                    "La suma de las piezas (%s kg) debe ser igual a los kilos de la compra (%s kg).",
                    formatKilograms(sum), formatKilograms(total)));
        }

        assertCutsFit(purchaseId, weights);

        // Se reemplaza el reparto completo: primero se borra el anterior (si lo habia)
        pieceWeightRepository.deleteByPurchaseId(purchaseId);

        // Si el usuario dejo el reparto parejo, no hay nada que guardar: no queda como "ajustada".
        if (PieceWeights.sameWeights(weights, PieceWeights.evenSplit(total, units))) {
            return;
        }

        List<PurchasePieceWeight> rows = new ArrayList<>();
        for (int index = 0; index < units; index++) {
            rows.add(PurchasePieceWeight.builder()
                    .purchase(purchase)
                    .pieceNumber(index + 1)
                    .kilograms(weights.get(index))
                    .build());
        }

        pieceWeightRepository.saveAll(rows);
    }

    @Override
    @Transactional
    public void updatePieceCuts(Long purchaseId, int pieceNumber, List<StockCutsRequestDto.Cut> cuts) {

        Purchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase", "id", purchaseId ));

        int units = purchase.getUnit() != null && purchase.getUnit() > 0
                ? purchase.getUnit()
                : 1;

        if (pieceNumber < 1 || pieceNumber > units) {
            throw new BusinessRuleException(String.format(
                    "La compra tiene %d unidades: la pieza %d no existe.", units, pieceNumber
            ));
        }

        List<Long> cutMeatIds = cuts.stream()
                .map(StockCutsRequestDto.Cut::cutMeatId)
                .distinct()
                .toList();

        Map<Long, CutMeat> cutMeats = cutMeatRepository.findAllById(cutMeatIds).stream()
                .collect(Collectors.toMap(CutMeat::getId, Function.identity()));

        for (Long cutMeatId: cutMeatIds) {
            CutMeat cutMeat = cutMeats.get(cutMeatId);

            if (cutMeat == null) {
                throw new ResourceNotFoundException("CutMeat","id", cutMeatId);
            }

            if (!Boolean.TRUE.equals(cutMeat.getIsActive())) {
                throw new BusinessRuleException(String.format("El corte «%s» no está disponible.", cutMeat.getCutMeatName()));
            }

            if (cutMeat.getSpecie() != purchase.getPurchaseType().getSpecie()) {
                throw new BusinessRuleException(String.format(
                        "El corte «%s» no corresponde a la especie de la compra.", cutMeat.getCutMeatName()
                ));
            }
        }

        // Peso de la pieza: el ajuste manual (si lo hay) o el reparto parejo
        List<PurchasePieceWeight> savedWeights = pieceWeightRepository.findByPurchaseIdOrderByPieceNumber(purchaseId);
        BigDecimal pieceKilograms = PieceWeights.resolve(units, purchase.getKilograms(), savedWeights).kilograms().get(pieceNumber -1);

        BigDecimal sum = cuts.stream()
                .map(cut -> Decimals.pieceKg(cut.kilograms()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Puede sumar menos: la diferencia es la merma
        if (sum.compareTo(pieceKilograms) > 0) {
            throw new BusinessRuleException(String.format(
                    "Los cortes suman %s kg y la pieza pesa %s kg.",
                    formatKilograms(sum), formatKilograms(pieceKilograms)
            ));
        }

        // Se reemplazan todos los cortes de la pieza
        stockCutRepository.deleteByPurchaseIdAndPieceNumber(purchaseId, pieceNumber);

        List<StockCut> rows = cuts.stream()
                .map(cut -> StockCut.builder()
                        .purchase(purchase)
                        .pieceNumber(pieceNumber)
                        .cutMeat(cutMeats.get(cut.cutMeatId()))
                        .kilograms(Decimals.pieceKg(cut.kilograms()))
                        .build())
                .toList();

        stockCutRepository.saveAll(rows);
    }

    // Los cortes ya cargados en cada pieza tienen que seguir entrando en su peso nuevo
    private void assertCutsFit(Long purchaseId, List<BigDecimal> weights) {

        Map<Integer, BigDecimal> cutKilogramsByPiece = stockCutRepository.findByPurchaseId(purchaseId).stream()
                .collect(Collectors.groupingBy(
                        StockCut::getPieceNumber,
                        Collectors.reducing(BigDecimal.ZERO, StockCut::getKilograms, BigDecimal::add)
                ));

        for (int index = 0; index < weights.size(); index++) {
            BigDecimal cutKilograms = cutKilogramsByPiece.getOrDefault(index+1, BigDecimal.ZERO);

            if (cutKilograms.compareTo(weights.get(index)) > 0) {
                throw new BusinessRuleException(String.format(
                        "La pieza %d tiene cortes por %s kg y no puede pesar menos (%s kg).",
                        index + 1, formatKilograms(cutKilograms), formatKilograms(weights.get(index))
                ));
            }
        }

    }

    // Cada compra se parte en tantas piezas como unidades tenga. El peso sale del ajuste
    // guardado (si lo hay) o de repartir los kilos en partes iguales.
    // Mantiene el orden de la consulta: compra mas reciente primero
    private List<StockItemDto> splitIntoItems(List<StockPurchaseDto> purchases) {

        Map<Long, List<PurchasePieceWeight>> savedByPurchase = loadSavedWeights(purchases);
        Map<Long, Map<Integer, List<StockItemDto.Cut>>> cutsByPurchase = loadCuts(purchases);
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
                        purchase.purchaseTypeId(),
                        purchase.purchaseTypeName(),
                        purchase.specie(),
                        distribution.kilograms().get(number - 1),
                        purchase.date(),
                        purchase.supplierId(),
                        purchase.supplierName(),
                        units > 1,
                        distribution.adjusted(),
                        cutsByPurchase.getOrDefault(purchase.id(), Map.of()).getOrDefault(number, List.of())
                        ));
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

    // Cortes de todas las compras del rango, agrupados por compra y por pieza, con una sola consulta
    private Map<Long, Map<Integer, List<StockItemDto.Cut>>> loadCuts(List<StockPurchaseDto> purchases) {

        if (purchases.isEmpty()) {
            return Map.of();
        }

        List<Long> purchaseIds = purchases.stream().map(StockPurchaseDto::id).toList();

        return stockCutRepository.findByPurchaseIdIn(purchaseIds).stream()
                .collect(Collectors.groupingBy(
                        cut -> cut.getPurchase().getId(),
                        Collectors.groupingBy(
                                StockCut::getPieceNumber,
                                Collectors.mapping(this::toCutDto, Collectors.toList()))
                ));

    }

    private StockItemDto.Cut toCutDto(StockCut cut) {
        return new StockItemDto.Cut(
                cut.getId(),
                cut.getCutMeat().getId(),
                cut.getCutMeat().getCutMeatName(),
                cut.getKilograms());
    }


    // Los totales salen de las compras (no de sumas las piezas), asi no se acumula error de redondeo
    private StockSummaryDto summarize(List<StockPurchaseDto> purchases) {

        // Un renglon por tipo de compra que tenga datos: primero bovinos, luego porcinos, por nombre
        List<StockSummaryDto.TypeTotals> byType = purchases.stream()
                .collect(Collectors.groupingBy(StockPurchaseDto::purchaseTypeId))
                .values().stream()
                    .map(ofType -> new StockSummaryDto.TypeTotals(
                            ofType.get(0).purchaseTypeId(),
                            ofType.get(0).purchaseTypeName(),
                            ofType.get(0).specie(),
                            ofType.stream().map(this::kilogramsOf).reduce(BigDecimal.ZERO, BigDecimal::add),
                            ofType.stream().mapToInt(this::unitsOf).sum()))
                .sorted(Comparator.comparing(StockSummaryDto.TypeTotals::specie)
                        .thenComparing(StockSummaryDto.TypeTotals::purchaseTypeName,
                                String.CASE_INSENSITIVE_ORDER)).toList();

        StockSummaryDto.SpecieTotals bovine = totalsOf(byType, Specie.BOVINE);
        StockSummaryDto.SpecieTotals porcine = totalsOf(byType, Specie.PORCINE);

        return new StockSummaryDto(
                bovine.kilograms().add(porcine.kilograms()),
                bovine.units() + porcine.units(),
                bovine,
                porcine,
                byType);
    }

    private StockSummaryDto.SpecieTotals totalsOf(List<StockSummaryDto.TypeTotals> byType, Specie specie) {
        BigDecimal kilograms = BigDecimal.ZERO;
        int units = 0;

        for (StockSummaryDto.TypeTotals totals: byType) {
            if (totals.specie() == specie) {
                kilograms = kilograms.add(totals.kilograms());
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

    private BigDecimal kilogramsOf(StockPurchaseDto purchase) {
        return purchase.kilograms() != null
                ? purchase.kilograms() : BigDecimal.ZERO;
    }

    // Coma decimal, como lo ve el usuario (210,00)
    private String formatKilograms(BigDecimal kilograms) {
        return String.format(Locale.forLanguageTag("es-AR"), "%.2f", kilograms);
    }
}

