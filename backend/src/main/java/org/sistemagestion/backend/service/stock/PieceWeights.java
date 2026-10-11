package org.sistemagestion.backend.service.stock;

import org.sistemagestion.backend.common.Decimals;
import org.sistemagestion.backend.model.PurchasePieceWeight;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// Reparto de los kilos de una compra entre sus piezas
public final class PieceWeights {

    private PieceWeights() {}

    // Pesos de cada pieza (en orden) y si vienen de un ajuste manual
    public record Distribution(List<BigDecimal> kilograms, boolean adjusted) {}

    // Usa los pesos guardados si siguen siendo validos para la compra; si no, reparto parejo.
    public static Distribution resolve(int units, BigDecimal kilograms, List<PurchasePieceWeight> saved) {

       BigDecimal total = Decimals.pieceKg(kilograms);

       if (isValid(saved, units, total)) {
           List<BigDecimal> weights = saved.stream()
                   .sorted(Comparator.comparing(PurchasePieceWeight::getPieceNumber))
                   .map(PurchasePieceWeight::getKilograms)
                   .toList();

           return new Distribution(weights, true);
       }

       return new Distribution(evenSplit(total, units), false);

    }

    // Partes iguales con 2 decimales: el resto de centavos va a la ultima pieza
    // para que la suma sea exactamente igual al total (100kg / 3) = 33,33 + 33,33 + 33,34)
    public static List<BigDecimal> evenSplit(BigDecimal kilograms, int units) {
        BigDecimal total = Decimals.pieceKg(kilograms);
        BigDecimal each = total.divide(BigDecimal.valueOf(units), Decimals.PIECE_SCALE, RoundingMode.DOWN);
        BigDecimal last = total.subtract(each.multiply(BigDecimal.valueOf(units - 1)));

        List<BigDecimal> weights = new ArrayList<>();
        for (int number = 1; number < units; number++) {
            weights.add(each);
        }

        weights.add(last);

        return weights;
    }

    // Igualdad por valor: BigDecimal.equals distingue 2.0 de 2.00
    public static boolean sameWeights(List<BigDecimal> first, List<BigDecimal> second) {

        if (first.size() != second.size()) {
            return false;
        }

        for (int index = 0; index < first.size(); index++) {
            if (first.get(index).compareTo(second.get(index)) != 0) {
                return false;
            }
        }

        return true;
    }

    // Un ajuste solo vale si tiene una fila por pieza (1..units) y suma lo mismo que la compra
    // Si la compra cambio de kilos o de unidades, el ajuste viejo queda descartado
    private static boolean isValid(List<PurchasePieceWeight> saved, int units, BigDecimal total) {

        if (saved == null || saved.size() != units) {
            return false;
        }

        List<Integer> numbers = saved.stream()
                .map(PurchasePieceWeight::getPieceNumber)
                .sorted()
                .toList();

        for (int index = 0; index < units; index++) {
            if (numbers.get(index) != index + 1) {
                return false;
            }
        }

        BigDecimal sum = saved.stream()
                .map(PurchasePieceWeight::getKilograms)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Exacto: en escala 2 no hace falta tolerancia de punto flotante
        return sum.compareTo(total) == 0;
    }
}
