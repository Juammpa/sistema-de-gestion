package org.sistemagestion.backend.common;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Decimals {

    public static final int MONEY_SCALE = 2;
    public static final int KG_SCALE = 3;
    public static final int PIECE_SCALE = 2;
    public static final RoundingMode MODE = RoundingMode.HALF_UP;
    public static final BigDecimal MONEY_ZERO = BigDecimal.ZERO.setScale(MONEY_SCALE);

    private Decimals() {}

    public static BigDecimal money(BigDecimal v) {
        return v == null ? null : v.setScale(MONEY_SCALE, MODE);
    }

    public static BigDecimal moneyOrZero(BigDecimal v) {
        return v == null ? MONEY_ZERO : v.setScale(MONEY_SCALE, MODE);
    }

    public static BigDecimal kilograms(BigDecimal v) {
        return v == null ? null : v.setScale(KG_SCALE, MODE);
    }

    public static BigDecimal pieceKg(BigDecimal v) {
        return v == null ? BigDecimal.ZERO.setScale(PIECE_SCALE) : v.setScale(PIECE_SCALE, MODE);
    }

    public static BigDecimal multiply(BigDecimal kilograms, BigDecimal pricePerKilo) {
        return kilograms.multiply(pricePerKilo).setScale(MONEY_SCALE, MODE);
    }

    // BigDecimal.equals distingue 2.0 de 2.00; esto compara por valor
    public static boolean sameValue(BigDecimal a, BigDecimal b) {
        if (a == null || b == null) return a == b;
        return a.compareTo(b) == 0;
    }

}
