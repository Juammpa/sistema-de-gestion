package org.sistemagestion.backend.service.stock;

import org.sistemagestion.backend.model.PurchasePieceWeight;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// Reparto de los kilos de una compra entre sus piezas
public final class PieceWeights {

    // Media centesima: absorbe el error de punto flotante al comparar sumas
    private static final double TOLERANCE = 0.005;

    private PieceWeights() {}

    // Pesos de cada pieza (en orden) y si vienen de un ajuste manual
    public record Distribution(List<Double> kilograms, boolean adjusted) {}

    // Usa los pesos guardados si siguen siendo validos para la compra; si no, reparto parejo.
    public static Distribution resolve(int units, double kilograms, List<PurchasePieceWeight> saved) {

       if (isValid(saved, units, kilograms)) {
           List<Double> weights = saved.stream()
                   .sorted(Comparator.comparing(PurchasePieceWeight::getPieceNumber))
                   .map(PurchasePieceWeight::getKilograms)
                   .toList();

           return new Distribution(weights, true);
       }

       return new Distribution(evenSplit(kilograms, units), true);

    }

    // Partes iguales con 2 decimales: el resto de centavos va a la ultima pieza
    // para que la suma sea exactamente igual al total (100kg / 3) = 33,33 + 33,33 + 33,34)
    public static List<Double> evenSplit(double kilograms, int units) {
        BigDecimal total = BigDecimal.valueOf(kilograms).setScale(2, RoundingMode.HALF_UP);
        BigDecimal each = total.divide(BigDecimal.valueOf(units), 2, RoundingMode.DOWN);
        BigDecimal last = total.subtract(each.multiply(BigDecimal.valueOf(units -1)));

        List<Double> weights = new ArrayList<>();
        for (int number = 1; number < units; number++) {
            weights.add(each.doubleValue());
        }

        weights.add(last.doubleValue());

        return weights;
    }

    // Un ajuste solo vale si tiene una fila por pieza (1..units) y suma lo mismo que la compra
    // Si la compra cambio de kilos o de unidades, el ajuste viejo queda descartado
    private static boolean isValid(List<PurchasePieceWeight> saved, int units, double kilograms) {

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

        double sum = saved.stream().mapToDouble(PurchasePieceWeight::getKilograms).sum();

        return Math.abs(sum - kilograms) <= TOLERANCE;
    }
}
