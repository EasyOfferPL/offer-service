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

    public static BigDecimal resolveSalary(OfferEntity offer) {
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
                minimum.add(maximum).divide(BigDecimal.valueOf(2), SALARY_SCALE, RoundingMode.HALF_UP)
        );
    }

    public static BigDecimal resolveMonthlySalary(OfferEntity offer) {
        BigDecimal salary = resolveSalary(offer);
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
                    salary.divide(BigDecimal.valueOf(12), SALARY_SCALE, RoundingMode.HALF_UP));
        };
    }

}
