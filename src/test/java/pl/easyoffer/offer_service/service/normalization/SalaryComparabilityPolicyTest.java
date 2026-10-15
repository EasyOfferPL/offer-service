package pl.easyoffer.offer_service.service.normalization;

import org.junit.jupiter.api.Test;
import pl.easyoffer.offer_service.model.SalaryType;
import pl.easyoffer.offer_service.service.normalization.SalaryComparabilityPolicy.ExclusionReason;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class SalaryComparabilityPolicyTest {

    private final SalaryComparabilityPolicy policy = new SalaryComparabilityPolicy();

    @Test
    void shouldNormalizeSupportedSalaryUnitsToMonthlyPln() {
        assertThat(assess("100", "100", "hour").monthlySalary())
                .isEqualByComparingTo("16800.00");
        assertThat(assess("800", "800", "day").monthlySalary())
                .isEqualByComparingTo("16800.00");
        assertThat(assess("16800", "16800", "month").monthlySalary())
                .isEqualByComparingTo("16800.00");
        assertThat(assess("201600", "201600", "year").monthlySalary())
                .isEqualByComparingTo("16800.00");
    }

    @Test
    void shouldUseSingleSalaryBoundaryAndNormalizeComparableDimensions() {
        var assessment = policy.assess(input(
                null, "120", " pln ", "hour", " b2b ", SalaryType.NET
        ));

        assertThat(assessment.usable()).isTrue();
        assertThat(assessment.singleBoundary()).isTrue();
        assertThat(assessment.monthlySalary()).isEqualByComparingTo("20160.00");
        assertThat(assessment.employmentType()).isEqualTo("B2B");
        assertThat(assessment.salaryType()).isEqualTo(SalaryType.NET);
    }

    @Test
    void shouldReturnOneExplicitReasonForEveryUnusableSalary() {
        assertThat(policy.assess(input(null, null, "PLN", "MONTH", "B2B", SalaryType.GROSS)).exclusionReason())
                .isEqualTo(ExclusionReason.MISSING_SALARY);
        assertThat(policy.assess(input("0", "100", "PLN", "MONTH", "B2B", SalaryType.GROSS)).exclusionReason())
                .isEqualTo(ExclusionReason.NON_POSITIVE_SALARY);
        assertThat(policy.assess(input("200", "100", "PLN", "MONTH", "B2B", SalaryType.GROSS)).exclusionReason())
                .isEqualTo(ExclusionReason.INVERTED_RANGE);
        assertThat(policy.assess(input("100", "200", "EUR", "MONTH", "B2B", SalaryType.GROSS)).exclusionReason())
                .isEqualTo(ExclusionReason.UNSUPPORTED_CURRENCY);
        assertThat(policy.assess(input("100", "200", "PLN", "WEEK", "B2B", SalaryType.GROSS)).exclusionReason())
                .isEqualTo(ExclusionReason.UNSUPPORTED_SALARY_UNIT);
        assertThat(policy.assess(input("100", "200", "PLN", "MONTH", " ", SalaryType.GROSS)).exclusionReason())
                .isEqualTo(ExclusionReason.MISSING_EMPLOYMENT_TYPE);
        assertThat(policy.assess(input("100", "200", "PLN", "MONTH", "B2B", SalaryType.UNKNOWN)).exclusionReason())
                .isEqualTo(ExclusionReason.UNKNOWN_SALARY_TYPE);
    }

    private SalaryComparabilityPolicy.SalaryComparabilityAssessment assess(
            String minimum,
            String maximum,
            String salaryUnit
    ) {
        return policy.assess(input(minimum, maximum, "PLN", salaryUnit, "B2B", SalaryType.GROSS));
    }

    private SalaryComparabilityInput input(
            String minimum,
            String maximum,
            String currency,
            String salaryUnit,
            String employmentType,
            SalaryType salaryType
    ) {
        return new SalaryComparabilityInput(
                decimal(minimum), decimal(maximum), currency, salaryUnit, employmentType, salaryType
        );
    }

    private BigDecimal decimal(String value) {
        return value == null ? null : new BigDecimal(value);
    }
}
