package pl.easyoffer.offer_service.service.calculator;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pl.easyoffer.offer_service.model.entity.OfferEntity;
import pl.easyoffer.offer_service.model.to.SalaryStatisticTO;
import pl.easyoffer.offer_service.util.SalaryUtil;

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
            .thenComparing(SalaryStatisticTO::getEmploymentType, NULL_SAFE_TEXT_ORDER);


    static List<SalaryStatisticTO> calculate(
            List<OfferEntity> offers,
            Function<OfferEntity, String> groupValueExtractor
    ) {
        return groupSalaries(offers, groupValueExtractor).entrySet().stream()
                .map(entry -> createStatistic(entry.getKey(), entry.getValue()))
                .sorted(STATISTIC_ORDER)
                .toList();
    }

    private static Map<SalaryGroup, List<BigDecimal>> groupSalaries(
            List<OfferEntity> offers,
            Function<OfferEntity, String> groupValueExtractor
    ) {
        Map<SalaryGroup, List<BigDecimal>> salariesByGroup = new HashMap<>();

        for (OfferEntity offer : offers) {
            BigDecimal representativeSalary = SalaryUtil.resolveRepresentativeSalary(offer);
            String groupValue = groupValueExtractor.apply(offer);
            if (representativeSalary == null || !hasText(groupValue)) {
                continue;
            }

            SalaryGroup group = SalaryGroup.from(groupValue, offer);
            salariesByGroup.computeIfAbsent(group, ignored -> new ArrayList<>()).add(representativeSalary);
        }

        return salariesByGroup;
    }

    private static SalaryStatisticTO createStatistic(SalaryGroup group, List<BigDecimal> salaries) {
        SalarySummary summary = SalarySummary.fromSalaries(salaries);

        return SalaryStatisticTO.builder()
                .groupValue(group.groupValue())
                .currency(group.currency())
                .salaryUnit(group.salaryUnit())
                .employmentType(group.employmentType())
                .averageSalary(summary.averageSalary())
                .medianSalary(summary.medianSalary())
                .offersCount(summary.offersCount())
                .build();
    }

    private record SalaryGroup(String groupValue, String currency, String salaryUnit, String employmentType) {

        private static SalaryGroup from(String groupValue, OfferEntity offer) {
            return new SalaryGroup(
                    groupValue,
                    offer.getCurrency(),
                    offer.getSalaryUnit(),
                    offer.getEmploymentType()
            );
        }
    }
}
