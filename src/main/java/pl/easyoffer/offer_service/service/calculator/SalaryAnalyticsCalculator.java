package pl.easyoffer.offer_service.service.calculator;

import org.springframework.stereotype.Component;
import pl.easyoffer.offer_service.model.entity.OfferEntity;
import pl.easyoffer.offer_service.model.to.SalaryStatisticTO;
import pl.easyoffer.offer_service.model.to.SalaryTrendPointTO;
import pl.easyoffer.offer_service.util.BigDecimalUtil;
import pl.easyoffer.offer_service.util.SalaryUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;

import static org.springframework.util.StringUtils.hasText;

@Component
public class SalaryAnalyticsCalculator {

    public List<SalaryStatisticTO> calculateByCategory(List<OfferEntity> offers) {
        return calculate(offers, OfferEntity::getCategory);
    }

    public List<SalaryStatisticTO> calculateByExperienceLevel(List<OfferEntity> offers) {
        return calculate(offers, OfferEntity::getExperienceLevel);
    }

    public List<SalaryTrendPointTO> calculateMonthlyTrend(
            List<OfferEntity> offers,
            LocalDateTime dateFrom,
            LocalDateTime dateTo
    ) {
        Map<YearMonth, List<BigDecimal>> salariesByMonth = new TreeMap<>();
        for (OfferEntity offer : offers) {
            BigDecimal monthlySalary = SalaryUtil.resolveMonthlySalary(offer);
            LocalDateTime publicationDate = offer.getPublishedAt() != null
                    ? offer.getPublishedAt()
                    : offer.getCreatedAt();
            if (monthlySalary != null && publicationDate != null) {
                salariesByMonth.computeIfAbsent(YearMonth.from(publicationDate), ignored -> new ArrayList<>())
                        .add(monthlySalary);
            }
        }

        List<SalaryTrendPointTO> points = new ArrayList<>();
        YearMonth lastMonth = YearMonth.from(dateTo);
        for (YearMonth month = YearMonth.from(dateFrom); !month.isAfter(lastMonth); month = month.plusMonths(1)) {
            List<BigDecimal> salaries = salariesByMonth.getOrDefault(month, List.of());
            points.add(toTrendPoint(month, salaries));
        }
        return points;
    }

    private List<SalaryStatisticTO> calculate(List<OfferEntity> offers, GroupValueExtractor groupValueExtractor) {
        Map<SalaryGroup, List<BigDecimal>> salariesByGroup = new HashMap<>();

        for (OfferEntity offer : offers) {
            BigDecimal salary = resolveSalary(offer);
            String groupValue = groupValueExtractor.extract(offer);
            if (salary == null || !hasText(groupValue)) {
                continue;
            }

            SalaryGroup group = new SalaryGroup(
                    groupValue,
                    offer.getCurrency(),
                    offer.getSalaryUnit(),
                    offer.getEmploymentType()
            );
            salariesByGroup.computeIfAbsent(group, ignored -> new ArrayList<>()).add(salary);
        }

        return salariesByGroup.entrySet().stream()
                .map(entry -> toSalaryStatistic(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(SalaryStatisticTO::getGroupValue)
                        .thenComparing(SalaryStatisticTO::getCurrency, Comparator.nullsFirst(String::compareTo))
                        .thenComparing(SalaryStatisticTO::getSalaryUnit, Comparator.nullsFirst(String::compareTo))
                        .thenComparing(SalaryStatisticTO::getEmploymentType, Comparator.nullsFirst(String::compareTo)))
                .toList();
    }

    private BigDecimal resolveSalary(OfferEntity offer) {
        return SalaryUtil.resolveSalary(offer);
    }

    private SalaryStatisticTO toSalaryStatistic(SalaryGroup group, List<BigDecimal> salaries) {
        List<BigDecimal> sortedSalaries = salaries.stream().sorted().toList();
        BigDecimal sum = sortedSalaries.stream().reduce(BigDecimal.ZERO, BigDecimal::add);

        return SalaryStatisticTO.builder()
                .groupValue(group.groupValue())
                .currency(group.currency())
                .salaryUnit(group.salaryUnit())
                .employmentType(group.employmentType())
                .averageSalary(BigDecimalUtil.scale(
                        SalaryUtil.SALARY_SCALE,
                        sum.divide(BigDecimal.valueOf(
                                sortedSalaries.size()), SalaryUtil.SALARY_SCALE, RoundingMode.HALF_UP)
                        )
                )
                .medianSalary(calculateMedian(sortedSalaries))
                .offersCount(sortedSalaries.size())
                .build();
    }

    private BigDecimal calculateMedian(List<BigDecimal> sortedSalaries) {
        int middle = sortedSalaries.size() / 2;
        if (sortedSalaries.size() % 2 == 1) {
            return BigDecimalUtil.scale(SalaryUtil.SALARY_SCALE, sortedSalaries.get(middle));
        }
        return BigDecimalUtil.scale(SalaryUtil.SALARY_SCALE, sortedSalaries.get(middle - 1).add(sortedSalaries.get(middle))
                .divide(BigDecimal.valueOf(2), SalaryUtil.SALARY_SCALE, RoundingMode.HALF_UP));
    }

    private SalaryTrendPointTO toTrendPoint(YearMonth month, List<BigDecimal> salaries) {
        if (salaries.isEmpty()) {
            return SalaryTrendPointTO.builder()
                    .month(month.toString())
                    .offersCount(0)
                    .build();
        }

        List<BigDecimal> sortedSalaries = salaries.stream().sorted().toList();
        BigDecimal sum = sortedSalaries.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return SalaryTrendPointTO.builder()
                .month(month.toString())
                .averageSalary(BigDecimalUtil.scale(
                        SalaryUtil.SALARY_SCALE,
                        sum.divide(BigDecimal.valueOf(sortedSalaries.size()), SalaryUtil.SALARY_SCALE, RoundingMode.HALF_UP)
                ))
                .medianSalary(calculateMedian(sortedSalaries))
                .offersCount(sortedSalaries.size())
                .build();
    }

    @FunctionalInterface
    private interface GroupValueExtractor {
        String extract(OfferEntity offer);
    }

    private record SalaryGroup(String groupValue, String currency, String salaryUnit, String employmentType) {
    }
}
