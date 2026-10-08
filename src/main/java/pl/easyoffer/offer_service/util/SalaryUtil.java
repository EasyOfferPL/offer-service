package pl.easyoffer.offer_service.util;

import lombok.experimental.UtilityClass;
import pl.easyoffer.offer_service.model.SalaryUnit;
import pl.easyoffer.offer_service.model.entity.OfferEntity;

import java.math.BigDecimal;
import java.math.RoundingMode;

@UtilityClass
public class SalaryUtil {

    public static final int SALARY_SCALE = 2;
    public static final int WORKING_DAYS_PER_MONTH = 21;
    public static final int WORKING_HOURS_PER_MONTH = 168;
    private static final BigDecimal SALARY_RANGE_ENDPOINT_COUNT = BigDecimal.valueOf(2);
    private static final BigDecimal MONTHS_PER_YEAR = BigDecimal.valueOf(12);
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    /**
     * @deprecated Use {@link #resolveRepresentativeSalary(OfferEntity)} to make the midpoint semantics explicit.
     */
    @Deprecated(forRemoval = false)
    public static BigDecimal resolveSalary(OfferEntity offer) {
        return resolveRepresentativeSalary(offer);
    }

    public static BigDecimal resolveRepresentativeSalary(OfferEntity offer) {
        BigDecimal minimum = offer.getSalaryMin();
        BigDecimal maximum = offer.getSalaryMax();
        if (minimum == null) {
            return BigDecimalUtil.scale(SALARY_SCALE, maximum);
        }
        if (maximum == null) {
            return BigDecimalUtil.scale(SALARY_SCALE, minimum);
        }
        return BigDecimalUtil.scale(
                SALARY_SCALE,
                minimum.add(maximum).divide(SALARY_RANGE_ENDPOINT_COUNT, SALARY_SCALE, ROUNDING_MODE)
        );
    }

    public static BigDecimal resolveMonthlySalary(OfferEntity offer) {
        BigDecimal salary = resolveRepresentativeSalary(offer);
        if (salary == null || offer.getSalaryUnit() == null) {
            return null;
        }
        SalaryUnit salaryUnit = EnumUtils.getEnum(SalaryUnit.class, offer.getSalaryUnit(), null);
        return switch (salaryUnit) {
            case HOUR -> BigDecimalUtil.scale(SALARY_SCALE,
                    salary.multiply(BigDecimal.valueOf(WORKING_HOURS_PER_MONTH)));
            case DAY -> BigDecimalUtil.scale(SALARY_SCALE,
                    salary.multiply(BigDecimal.valueOf(WORKING_DAYS_PER_MONTH)));
            case MONTH -> salary;
            case YEAR -> BigDecimalUtil.scale(SALARY_SCALE,
                    salary.divide(MONTHS_PER_YEAR, SALARY_SCALE, ROUNDING_MODE));
        };
    }

}
