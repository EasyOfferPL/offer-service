package pl.easyoffer.offer_service.service.calculator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.easyoffer.offer_service.model.to.SalaryComparabilityAuditTO;
import pl.easyoffer.offer_service.service.normalization.SalaryComparabilityInput;
import pl.easyoffer.offer_service.service.normalization.SalaryComparabilityPolicy;
import pl.easyoffer.offer_service.service.normalization.SalaryComparabilityPolicy.ExclusionReason;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class SalaryComparabilityAuditCalculator {

    private final SalaryComparabilityPolicy policy;

    public SalaryComparabilityAuditTO calculate(Stream<SalaryComparabilityInput> inputs) {
        EnumMap<ExclusionReason, Long> exclusions = new EnumMap<>(ExclusionReason.class);
        long rawOfferCount = 0;
        long usableOfferCount = 0;
        long usableSingleBoundaryOfferCount = 0;

        var iterator = inputs.iterator();
        while (iterator.hasNext()) {
            rawOfferCount++;
            var assessment = policy.assess(iterator.next());
            if (assessment.usable()) {
                usableOfferCount++;
                if (assessment.singleBoundary()) {
                    usableSingleBoundaryOfferCount++;
                }
            } else {
                exclusions.merge(assessment.exclusionReason(), 1L, Long::sum);
            }
        }

        Map<String, Long> orderedExclusions = new LinkedHashMap<>();
        for (ExclusionReason reason : ExclusionReason.values()) {
            orderedExclusions.put(reason.name(), exclusions.getOrDefault(reason, 0L));
        }
        long missingSalaryCount = exclusions.getOrDefault(ExclusionReason.MISSING_SALARY, 0L);

        return SalaryComparabilityAuditTO.builder()
                .normalizationVersion(policy.version())
                .rawOfferCount(rawOfferCount)
                .salaryPresentOfferCount(rawOfferCount - missingSalaryCount)
                .usableOfferCount(usableOfferCount)
                .excludedOfferCount(rawOfferCount - usableOfferCount)
                .usableSingleBoundaryOfferCount(usableSingleBoundaryOfferCount)
                .exclusionCounts(orderedExclusions)
                .build();
    }
}
