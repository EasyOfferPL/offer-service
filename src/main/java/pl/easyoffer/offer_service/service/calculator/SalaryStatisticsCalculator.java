package pl.easyoffer.offer_service.service.calculator;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pl.easyoffer.offer_service.model.SalaryType;
import pl.easyoffer.offer_service.model.entity.OfferEntity;
import pl.easyoffer.offer_service.model.to.SalaryStatisticTO;
import pl.easyoffer.offer_service.service.normalization.SalaryComparabilityInput;
import pl.easyoffer.offer_service.service.normalization.SalaryComparabilityPolicy;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;

import static org.springframework.util.StringUtils.hasText;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class SalaryStatisticsCalculator {

    private static final Comparator<String> NULL_SAFE_TEXT_ORDER = Comparator.nullsFirst(String::compareTo);
    private static final Comparator<SalaryStatisticTO> STATISTIC_ORDER = Comparator
            .comparing(SalaryStatisticTO::getGroupValue)
            .thenComparing(SalaryStatisticTO::getCurrency, NULL_SAFE_TEXT_ORDER)
            .thenComparing(SalaryStatisticTO::getSalaryUnit, NULL_SAFE_TEXT_ORDER)
            .thenComparing(SalaryStatisticTO::getEmploymentType, NULL_SAFE_TEXT_ORDER)
            .thenComparing(SalaryStatisticTO::getSalaryType);


    static List<SalaryStatisticTO> calculate(
            List<OfferEntity> offers,
            Function<OfferEntity, String> groupValueExtractor,
            SalaryComparabilityPolicy comparabilityPolicy
    ) {
        return groupSalaries(offers, groupValueExtractor, comparabilityPolicy).entrySet().stream()
                .map(entry -> createStatistic(entry.getKey(), entry.getValue()))
                .sorted(STATISTIC_ORDER)
                .toList();
    }

    private static Map<SalaryGroup, List<BigDecimal>> groupSalaries(
            List<OfferEntity> offers,
            Function<OfferEntity, String> groupValueExtractor,
            SalaryComparabilityPolicy comparabilityPolicy
    ) {
        Map<SalaryGroup, List<BigDecimal>> salariesByGroup = new HashMap<>();

        for (OfferEntity offer : offers) {
            String groupValue = groupValueExtractor.apply(offer);
            var assessment = comparabilityPolicy.assess(SalaryComparabilityInput.from(offer));
            if (!assessment.usable() || !hasText(groupValue)) {
                continue;
            }

            SalaryGroup group = new SalaryGroup(
                    groupValue,
                    assessment.employmentType(),
                    assessment.salaryType()
            );
            salariesByGroup.computeIfAbsent(group, ignored -> new ArrayList<>())
                    .add(assessment.monthlySalary());
        }

        return salariesByGroup;
    }

    private static SalaryStatisticTO createStatistic(SalaryGroup group, List<BigDecimal> salaries) {
        SalarySummary summary = SalarySummary.fromSalaries(salaries);

        return SalaryStatisticTO.builder()
                .groupValue(group.groupValue())
                .currency(SalaryComparabilityPolicy.COMPARABLE_CURRENCY)
                .salaryUnit(SalaryComparabilityPolicy.COMPARABLE_SALARY_UNIT)
                .employmentType(group.employmentType())
                .salaryType(group.salaryType())
                .minimumSalary(summary.minimumSalary())
                .percentile25Salary(summary.percentile25Salary())
                .averageSalary(summary.averageSalary())
                .medianSalary(summary.medianSalary())
                .percentile75Salary(summary.percentile75Salary())
                .maximumSalary(summary.maximumSalary())
                .offersCount(summary.offersCount())
                .meetsRecommendedSampleSize(
                        summary.offersCount() >= SalarySummary.RECOMMENDED_MINIMUM_SAMPLE_SIZE
                )
                .build();
    }

    private record SalaryGroup(
            String groupValue,
            String employmentType,
            SalaryType salaryType
    ) {
    }
}
