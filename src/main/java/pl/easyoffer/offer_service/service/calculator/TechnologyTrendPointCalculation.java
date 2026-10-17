package pl.easyoffer.offer_service.service.calculator;

import java.time.YearMonth;

public record TechnologyTrendPointCalculation(
        YearMonth month,
        long count,
        long denominator
) {
}
