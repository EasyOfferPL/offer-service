package pl.easyoffer.offer_service.service.calculator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.easyoffer.offer_service.model.entity.OfferEntity;
import pl.easyoffer.offer_service.model.to.SalaryStatisticTO;
import pl.easyoffer.offer_service.model.to.SalaryTrendPointTO;
import pl.easyoffer.offer_service.service.normalization.SalaryComparabilityPolicy;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SalaryAnalyticsCalculator {

    private final SalaryComparabilityPolicy comparabilityPolicy;

    public String normalizationVersion() {
        return comparabilityPolicy.version();
    }

    public String quantileMethod() {
        return SalarySummary.QUANTILE_METHOD;
    }

    public int recommendedMinimumSampleSize() {
        return SalarySummary.RECOMMENDED_MINIMUM_SAMPLE_SIZE;
    }

    public List<SalaryStatisticTO> calculateByCategory(List<OfferEntity> offers) {
        return SalaryStatisticsCalculator.calculate(
                offers, OfferEntity::getCategory, comparabilityPolicy
        );
    }

    public List<SalaryStatisticTO> calculateByExperienceLevel(List<OfferEntity> offers) {
        return SalaryStatisticsCalculator.calculate(
                offers, OfferEntity::getExperienceLevel, comparabilityPolicy
        );
    }

    public List<SalaryTrendPointTO> calculateMonthlyTrend(
            List<OfferEntity> offers,
            LocalDate dateFrom,
            LocalDate dateTo
    ) {
        return SalaryTrendCalculator.calculate(
                offers, dateFrom, dateTo, comparabilityPolicy
        );
    }
}
