package pl.easyoffer.offer_service.service.calculator;

import pl.easyoffer.offer_service.service.normalization.SalaryComparabilityInput;

import java.time.LocalDateTime;

public record DataCompletenessInput(
        String source,
        SalaryComparabilityInput salary,
        long technologyCount,
        String experienceLevel,
        String location,
        String workMode,
        LocalDateTime publishedAt
) {
}
