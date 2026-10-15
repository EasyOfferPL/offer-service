package pl.easyoffer.offer_service.service.normalization;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import pl.easyoffer.offer_service.model.SalaryType;
import pl.easyoffer.offer_service.model.SalaryUnit;
import pl.easyoffer.offer_service.util.EnumUtils;
import pl.easyoffer.offer_service.util.SalaryUtil;

import java.math.BigDecimal;
import java.util.Locale;

@Component
public class SalaryComparabilityPolicy {

    public static final String VERSION = "v1";
    public static final String COMPARABLE_CURRENCY = "PLN";
    public static final String COMPARABLE_SALARY_UNIT = SalaryUnit.MONTH.name();

    public SalaryComparabilityAssessment assess(SalaryComparabilityInput input) {
        BigDecimal minimum = input.salaryMin();
        BigDecimal maximum = input.salaryMax();
        if (minimum == null && maximum == null) {
            return SalaryComparabilityAssessment.excluded(ExclusionReason.MISSING_SALARY);
        }
        if (isNonPositive(minimum) || isNonPositive(maximum)) {
            return SalaryComparabilityAssessment.excluded(ExclusionReason.NON_POSITIVE_SALARY);
        }
        if (minimum != null && maximum != null && minimum.compareTo(maximum) > 0) {
            return SalaryComparabilityAssessment.excluded(ExclusionReason.INVERTED_RANGE);
        }
        if (!COMPARABLE_CURRENCY.equals(normalize(input.currency()))) {
            return SalaryComparabilityAssessment.excluded(ExclusionReason.UNSUPPORTED_CURRENCY);
        }

        SalaryUnit salaryUnit = EnumUtils.getEnum(SalaryUnit.class, input.salaryUnit(), null);
        if (salaryUnit == null) {
            return SalaryComparabilityAssessment.excluded(ExclusionReason.UNSUPPORTED_SALARY_UNIT);
        }
        if (!StringUtils.hasText(input.employmentType())) {
            return SalaryComparabilityAssessment.excluded(ExclusionReason.MISSING_EMPLOYMENT_TYPE);
        }

        SalaryType salaryType = input.salaryType() == null ? SalaryType.UNKNOWN : input.salaryType();
        if (salaryType == SalaryType.UNKNOWN) {
            return SalaryComparabilityAssessment.excluded(ExclusionReason.UNKNOWN_SALARY_TYPE);
        }

        BigDecimal monthlySalary = SalaryUtil.resolveMonthlySalary(
                minimum, maximum, salaryUnit
        );
        return SalaryComparabilityAssessment.usable(
                monthlySalary,
                normalize(input.employmentType()),
                salaryType,
                minimum == null || maximum == null
        );
    }

    public String version() {
        return VERSION;
    }

    private static boolean isNonPositive(BigDecimal value) {
        return value != null && value.signum() <= 0;
    }

    private static String normalize(String value) {
        return value == null ? null : value.trim().toUpperCase(Locale.ROOT);
    }

    public enum ExclusionReason {
        MISSING_SALARY,
        NON_POSITIVE_SALARY,
        INVERTED_RANGE,
        UNSUPPORTED_CURRENCY,
        UNSUPPORTED_SALARY_UNIT,
        MISSING_EMPLOYMENT_TYPE,
        UNKNOWN_SALARY_TYPE
    }

    public record SalaryComparabilityAssessment(
            ExclusionReason exclusionReason,
            BigDecimal monthlySalary,
            String employmentType,
            SalaryType salaryType,
            boolean singleBoundary
    ) {

        public static SalaryComparabilityAssessment excluded(ExclusionReason reason) {
            return new SalaryComparabilityAssessment(reason, null, null, null, false);
        }

        public static SalaryComparabilityAssessment usable(
                BigDecimal monthlySalary,
                String employmentType,
                SalaryType salaryType,
                boolean singleBoundary
        ) {
            return new SalaryComparabilityAssessment(
                    null, monthlySalary, employmentType, salaryType, singleBoundary
            );
        }

        public boolean usable() {
            return exclusionReason == null;
        }
    }
}
