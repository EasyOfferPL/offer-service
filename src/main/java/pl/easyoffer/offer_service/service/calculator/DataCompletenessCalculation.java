package pl.easyoffer.offer_service.service.calculator;

import pl.easyoffer.offer_service.model.to.DataCompletenessSourceTO;

import java.util.List;

public record DataCompletenessCalculation(
        long eligibleOfferCount,
        List<DataCompletenessSourceTO> bySource
) {
}
