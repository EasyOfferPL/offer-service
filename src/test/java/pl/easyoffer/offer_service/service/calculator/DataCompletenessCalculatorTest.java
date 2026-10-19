package pl.easyoffer.offer_service.service.calculator;

import org.junit.jupiter.api.Test;
import pl.easyoffer.offer_service.model.DataCompletenessField;
import pl.easyoffer.offer_service.model.SalaryType;
import pl.easyoffer.offer_service.model.to.DataCompletenessFieldTO;
import pl.easyoffer.offer_service.model.to.DataCompletenessSourceTO;
import pl.easyoffer.offer_service.service.normalization.SalaryComparabilityInput;
import pl.easyoffer.offer_service.service.normalization.SalaryComparabilityPolicy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class DataCompletenessCalculatorTest {

    private static final LocalDateTime PUBLISHED_AT = LocalDateTime.of(2026, 10, 19, 12, 0);

    private final DataCompletenessCalculator calculator = new DataCompletenessCalculator(
            new SalaryComparabilityPolicy()
    );

    @Test
    void shouldCalculateUsableCountsAndPercentagesBySource() {
        DataCompletenessCalculation calculation = calculator.calculate(Stream.of(
                input(
                        "THE_PROTOCOL_IT", salary("10000", "14000", "PLN"),
                        1, "SENIOR", "WARSAW", "REMOTE", PUBLISHED_AT
                ),
                input(
                        "JUST_JOIN_IT", salary("10000", "14000", "PLN"),
                        2, "SENIOR", "WARSAW", "REMOTE", PUBLISHED_AT
                ),
                input(
                        "JUST_JOIN_IT", salary("3000", "4000", "EUR"),
                        0, " ", null, "HYBRID", null
                )
        ));

        assertThat(calculation.eligibleOfferCount()).isEqualTo(3);
        assertThat(calculation.bySource())
                .extracting(DataCompletenessSourceTO::getSource, DataCompletenessSourceTO::getEligibleOfferCount)
                .containsExactly(
                        tuple("JUST_JOIN_IT", 2L),
                        tuple("THE_PROTOCOL_IT", 1L)
                );

        DataCompletenessSourceTO justJoinIt = calculation.bySource().getFirst();
        assertField(justJoinIt, DataCompletenessField.SALARY, 1, 1, "50.0000");
        assertField(justJoinIt, DataCompletenessField.TECHNOLOGIES, 1, 1, "50.0000");
        assertField(justJoinIt, DataCompletenessField.EXPERIENCE_LEVEL, 1, 1, "50.0000");
        assertField(justJoinIt, DataCompletenessField.LOCATION, 1, 1, "50.0000");
        assertField(justJoinIt, DataCompletenessField.WORK_MODE, 2, 0, "100.0000");
        assertField(justJoinIt, DataCompletenessField.PUBLISHED_AT, 1, 1, "50.0000");

        DataCompletenessSourceTO theProtocol = calculation.bySource().get(1);
        assertThat(theProtocol.getFields())
                .allSatisfy(field -> {
                    assertThat(field.getUsableOfferCount()).isEqualTo(1);
                    assertThat(field.getUnusableOfferCount()).isZero();
                    assertThat(field.getEligibleOfferCount()).isEqualTo(1);
                    assertThat(field.getCompletenessPercentage()).isEqualByComparingTo("100.0000");
                });
    }

    private static void assertField(
            DataCompletenessSourceTO source,
            DataCompletenessField field,
            long usableCount,
            long unusableCount,
            String percentage
    ) {
        DataCompletenessFieldTO statistic = source.getFields().stream()
                .filter(item -> item.getField() == field)
                .findFirst()
                .orElseThrow();
        assertThat(statistic.getUsableOfferCount()).isEqualTo(usableCount);
        assertThat(statistic.getUnusableOfferCount()).isEqualTo(unusableCount);
        assertThat(statistic.getEligibleOfferCount()).isEqualTo(source.getEligibleOfferCount());
        assertThat(statistic.getCompletenessPercentage()).isEqualByComparingTo(percentage);
    }

    private static DataCompletenessInput input(
            String source,
            SalaryComparabilityInput salary,
            long technologyCount,
            String experienceLevel,
            String location,
            String workMode,
            LocalDateTime publishedAt
    ) {
        return new DataCompletenessInput(
                source, salary, technologyCount, experienceLevel, location, workMode, publishedAt
        );
    }

    private static SalaryComparabilityInput salary(String minimum, String maximum, String currency) {
        return new SalaryComparabilityInput(
                new BigDecimal(minimum),
                new BigDecimal(maximum),
                currency,
                "MONTH",
                "B2B",
                SalaryType.GROSS
        );
    }
}
