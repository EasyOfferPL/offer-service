package pl.easyoffer.offer_service.service.calculator;

import pl.easyoffer.offer_service.model.entity.OfferEntity;
import pl.easyoffer.offer_service.model.to.SalaryTrendPointTO;
import pl.easyoffer.offer_service.util.SalaryUtil;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

final class SalaryTrendCalculator {

    private SalaryTrendCalculator() {
    }

    static List<SalaryTrendPointTO> calculate(
            List<OfferEntity> offers,
            LocalDateTime dateFrom,
            LocalDateTime dateTo
    ) {
        Map<YearMonth, List<BigDecimal>> salariesByMonth = groupSalariesByMonth(offers);
        return buildMonthlyTrend(
                salariesByMonth,
                YearMonth.from(dateFrom),
                YearMonth.from(dateTo)
        );
    }

    private static Map<YearMonth, List<BigDecimal>> groupSalariesByMonth(List<OfferEntity> offers) {
        Map<YearMonth, List<BigDecimal>> salariesByMonth = new TreeMap<>();

        for (OfferEntity offer : offers) {
            BigDecimal monthlySalary = SalaryUtil.resolveMonthlySalary(offer);
            LocalDateTime publicationOrCreationDate = resolvePublicationOrCreationDate(offer);
            if (monthlySalary == null || publicationOrCreationDate == null) {
                continue;
            }

            YearMonth publicationMonth = YearMonth.from(publicationOrCreationDate);
            salariesByMonth.computeIfAbsent(publicationMonth, ignored -> new ArrayList<>())
                    .add(monthlySalary);
        }

        return salariesByMonth;
    }

    private static LocalDateTime resolvePublicationOrCreationDate(OfferEntity offer) {
        return offer.getPublishedAt() != null ? offer.getPublishedAt() : offer.getCreatedAt();
    }

    private static List<SalaryTrendPointTO> buildMonthlyTrend(
            Map<YearMonth, List<BigDecimal>> salariesByMonth,
            YearMonth firstMonth,
            YearMonth lastMonth
    ) {
        List<SalaryTrendPointTO> trend = new ArrayList<>();

        for (YearMonth month = firstMonth; !month.isAfter(lastMonth); month = month.plusMonths(1)) {
            trend.add(createTrendPoint(month, salariesByMonth.getOrDefault(month, List.of())));
        }

        return trend;
    }

    private static SalaryTrendPointTO createTrendPoint(YearMonth month, List<BigDecimal> salaries) {
        if (salaries.isEmpty()) {
            return SalaryTrendPointTO.builder()
                    .month(month.toString())
                    .offersCount(0)
                    .build();
        }

        SalarySummary summary = SalarySummary.fromSalaries(salaries);
        return SalaryTrendPointTO.builder()
                .month(month.toString())
                .averageSalary(summary.averageSalary())
                .medianSalary(summary.medianSalary())
                .offersCount(summary.offersCount())
                .build();
    }
}
