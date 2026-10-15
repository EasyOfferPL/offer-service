package pl.easyoffer.offer_service.service.normalization;

import pl.easyoffer.offer_service.model.SalaryType;
import pl.easyoffer.offer_service.model.entity.OfferEntity;

import java.math.BigDecimal;

public record SalaryComparabilityInput(
        BigDecimal salaryMin,
        BigDecimal salaryMax,
        String currency,
        String salaryUnit,
        String employmentType,
        SalaryType salaryType
) {

    public static SalaryComparabilityInput from(OfferEntity offer) {
        return new SalaryComparabilityInput(
                offer.getSalaryMin(),
                offer.getSalaryMax(),
                offer.getCurrency(),
                offer.getSalaryUnit(),
                offer.getEmploymentType(),
                offer.getSalaryType()
        );
    }
}
