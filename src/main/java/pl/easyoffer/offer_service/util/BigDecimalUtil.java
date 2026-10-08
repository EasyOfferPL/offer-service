package pl.easyoffer.offer_service.util;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.RoundingMode;

@UtilityClass
public class BigDecimalUtil {

    public static BigDecimal scale(int scale, BigDecimal value) {
        return value == null ? null : value.setScale(scale, RoundingMode.HALF_UP);
    }

}
