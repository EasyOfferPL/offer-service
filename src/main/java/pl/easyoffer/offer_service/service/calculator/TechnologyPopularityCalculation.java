package pl.easyoffer.offer_service.service.calculator;

import java.util.Map;

public record TechnologyPopularityCalculation(
        long rawOfferCount,
        long uniqueOfferCount,
        long eligibleOfferCount,
        Map<String, Long> offerCountsByTechnology
) {
}
