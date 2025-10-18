package com.ingemark.product_app.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class CurrencyConversionUtil {

    private CurrencyConversionUtil() {}

    public static Float convert(BigDecimal amount, BigDecimal exchangeRate){
        if(exchangeRate == null || amount == null ||
                amount.compareTo(BigDecimal.ZERO) == 0 ||
                exchangeRate.compareTo(BigDecimal.ZERO) == 0){
            return null;
        }

        return amount.multiply(exchangeRate).setScale(2, RoundingMode.HALF_UP).floatValue();
    }
}
