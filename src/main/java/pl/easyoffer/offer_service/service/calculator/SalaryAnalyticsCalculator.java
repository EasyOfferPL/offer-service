package pl.easyoffer.offer_service.service.calculator;

import org.springframework.stereotype.Component;
import pl.easyoffer.offer_service.model.entity.OfferEntity;
import pl.easyoffer.offer_service.model.to.SalaryStatisticTO;
import pl.easyoffer.offer_service.model.to.SalaryTrendPointTO;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class SalaryAnalyticsCalculator {

    public List<SalaryStatisticTO> calculateByCategory(List<OfferEntity> offers) {
        return SalaryStatisticsCalculator.calculate(offers, OfferEntity::getCategory);
    }

    public List<SalaryStatisticTO> calculateByExperienceLevel(List<OfferEntity> offers) {
        return SalaryStatisticsCalculator.calculate(offers, OfferEntity::getExperienceLevel);
    }

    public List<SalaryTrendPointTO> calculateMonthlyTrend(
            List<OfferEntity> offers,
            LocalDateTime dateFrom,
            LocalDateTime dateTo
    ) {
        return SalaryTrendCalculator.calculate(offers, dateFrom, dateTo);
    }
}
