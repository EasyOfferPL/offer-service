package pl.easyoffer.offer_service.service.calculator;

import java.util.List;

public record TechnologyTrendCalculation(
        long rawOfferCount,
        long uniqueOfferCount,
        long eligibleOfferCount,
        List<TechnologyTrendPointCalculation> points
) {
}
