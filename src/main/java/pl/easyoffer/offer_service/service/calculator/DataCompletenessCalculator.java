package pl.easyoffer.offer_service.service.calculator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import pl.easyoffer.offer_service.model.DataCompletenessField;
import pl.easyoffer.offer_service.model.to.DataCompletenessFieldTO;
import pl.easyoffer.offer_service.model.to.DataCompletenessSourceTO;
import pl.easyoffer.offer_service.service.normalization.SalaryComparabilityPolicy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class DataCompletenessCalculator {

    public static final String VERSION = "v1";
    public static final String PERIOD_FIELD = "CREATED_AT";
    public static final String UNIT_OF_ANALYSIS = "SOURCE_OFFER_ROW";

    private static final int PERCENTAGE_SCALE = 4;

    private final SalaryComparabilityPolicy salaryComparabilityPolicy;

    public DataCompletenessCalculation calculate(Stream<DataCompletenessInput> inputs) {
        Map<String, SourceCompletenessAccumulator> completenessBySource = new TreeMap<>();
        inputs.forEach(input -> completenessBySource
                .computeIfAbsent(input.source(), ignored -> new SourceCompletenessAccumulator())
                .accept(input));

        return new DataCompletenessCalculation(
                completenessBySource.values().stream()
                        .mapToLong(SourceCompletenessAccumulator::eligibleOfferCount)
                        .sum(),
                completenessBySource.entrySet().stream()
                        .map(entry -> entry.getValue().toTransferObject(entry.getKey()))
                        .toList()
        );
    }

    public String salaryNormalizationVersion() {
        return salaryComparabilityPolicy.version();
    }

    private final class SourceCompletenessAccumulator {

        private final EnumMap<DataCompletenessField, Long> usableCounts = new EnumMap<>(DataCompletenessField.class);
        private long eligibleOfferCount;

        private void accept(DataCompletenessInput input) {
            eligibleOfferCount++;
            countIfUsable(DataCompletenessField.SALARY, salaryComparabilityPolicy.assess(input.salary()).usable());
            countIfUsable(DataCompletenessField.TECHNOLOGIES, input.technologyCount() > 0);
            countIfUsable(DataCompletenessField.EXPERIENCE_LEVEL, StringUtils.hasText(input.experienceLevel()));
            countIfUsable(DataCompletenessField.LOCATION, StringUtils.hasText(input.location()));
            countIfUsable(DataCompletenessField.WORK_MODE, StringUtils.hasText(input.workMode()));
            countIfUsable(DataCompletenessField.PUBLISHED_AT, input.publishedAt() != null);
        }

        private void countIfUsable(DataCompletenessField field, boolean usable) {
            if (usable) {
                usableCounts.merge(field, 1L, Long::sum);
            }
        }

        private long eligibleOfferCount() {
            return eligibleOfferCount;
        }

        private DataCompletenessSourceTO toTransferObject(String source) {
            return DataCompletenessSourceTO.builder()
                    .source(source)
                    .eligibleOfferCount(eligibleOfferCount)
                    .fields(Stream.of(DataCompletenessField.values())
                            .map(this::toFieldTransferObject)
                            .toList())
                    .build();
        }

        private DataCompletenessFieldTO toFieldTransferObject(DataCompletenessField field) {
            long usableOfferCount = usableCounts.getOrDefault(field, 0L);
            return DataCompletenessFieldTO.builder()
                    .field(field)
                    .usableOfferCount(usableOfferCount)
                    .unusableOfferCount(eligibleOfferCount - usableOfferCount)
                    .eligibleOfferCount(eligibleOfferCount)
                    .completenessPercentage(calculatePercentage(usableOfferCount, eligibleOfferCount))
                    .build();
        }
    }

    private static BigDecimal calculatePercentage(long usableCount, long eligibleCount) {
        if (eligibleCount == 0) {
            return BigDecimal.ZERO.setScale(PERCENTAGE_SCALE);
        }
        return BigDecimal.valueOf(usableCount)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(eligibleCount), PERCENTAGE_SCALE, RoundingMode.HALF_UP);
    }
}
