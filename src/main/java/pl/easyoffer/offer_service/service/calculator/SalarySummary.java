package pl.easyoffer.offer_service.service.calculator;

import pl.easyoffer.offer_service.util.BigDecimalUtil;
import pl.easyoffer.offer_service.util.SalaryUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

record SalarySummary(BigDecimal averageSalary, BigDecimal medianSalary, int offersCount) {

    private static final int MEDIAN_PAIR_SIZE = 2;
    private static final BigDecimal MEDIAN_PAIR_DIVISOR = BigDecimal.valueOf(MEDIAN_PAIR_SIZE);
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    static SalarySummary fromSalaries(List<BigDecimal> salaries) {
        List<BigDecimal> sortedSalaries = salaries.stream().sorted().toList();
        return new SalarySummary(
                calculateAverage(sortedSalaries),
                calculateMedian(sortedSalaries),
                sortedSalaries.size()
        );
    }

    private static BigDecimal calculateAverage(List<BigDecimal> salaries) {
        BigDecimal sum = salaries.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal average = sum.divide(
                BigDecimal.valueOf(salaries.size()),
                SalaryUtil.SALARY_SCALE,
                ROUNDING_MODE
        );
        return BigDecimalUtil.scale(SalaryUtil.SALARY_SCALE, average);
    }

    private static BigDecimal calculateMedian(List<BigDecimal> sortedSalaries) {
        int middleIndex = sortedSalaries.size() / MEDIAN_PAIR_SIZE;
        if (hasOddSize(sortedSalaries)) {
            return BigDecimalUtil.scale(SalaryUtil.SALARY_SCALE, sortedSalaries.get(middleIndex));
        }

        BigDecimal middleValuesSum = sortedSalaries.get(middleIndex - 1)
                .add(sortedSalaries.get(middleIndex));
        BigDecimal median = middleValuesSum.divide(
                MEDIAN_PAIR_DIVISOR,
                SalaryUtil.SALARY_SCALE,
                ROUNDING_MODE
        );
        return BigDecimalUtil.scale(SalaryUtil.SALARY_SCALE, median);
    }

    private static boolean hasOddSize(List<BigDecimal> salaries) {
        return salaries.size() % MEDIAN_PAIR_SIZE != 0;
    }
}
