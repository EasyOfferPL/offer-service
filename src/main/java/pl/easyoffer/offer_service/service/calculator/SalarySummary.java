package pl.easyoffer.offer_service.service.calculator;

import pl.easyoffer.offer_service.util.BigDecimalUtil;
import pl.easyoffer.offer_service.util.SalaryUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

record SalarySummary(
        BigDecimal minimumSalary,
        BigDecimal percentile25Salary,
        BigDecimal averageSalary,
        BigDecimal medianSalary,
        BigDecimal percentile75Salary,
        BigDecimal maximumSalary,
        int offersCount
) {

    static final String QUANTILE_METHOD = "R7_LINEAR_INTERPOLATION";
    static final int RECOMMENDED_MINIMUM_SAMPLE_SIZE = 30;

    private static final BigDecimal PERCENTILE_25 = new BigDecimal("0.25");
    private static final BigDecimal PERCENTILE_75 = new BigDecimal("0.75");
    private static final int MEDIAN_PAIR_SIZE = 2;
    private static final BigDecimal MEDIAN_PAIR_DIVISOR = BigDecimal.valueOf(MEDIAN_PAIR_SIZE);
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    static SalarySummary fromSalaries(List<BigDecimal> salaries) {
        List<BigDecimal> sortedSalaries = salaries.stream().sorted().toList();
        return new SalarySummary(
                scaledValueAt(sortedSalaries, 0),
                calculateR7Percentile(sortedSalaries, PERCENTILE_25),
                calculateAverage(sortedSalaries),
                calculateMedian(sortedSalaries),
                calculateR7Percentile(sortedSalaries, PERCENTILE_75),
                scaledValueAt(sortedSalaries, sortedSalaries.size() - 1),
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

    private static BigDecimal calculateR7Percentile(
            List<BigDecimal> sortedSalaries,
            BigDecimal percentile
    ) {
        BigDecimal zeroBasedPosition = BigDecimal.valueOf(sortedSalaries.size() - 1)
                .multiply(percentile);
        int lowerIndex = zeroBasedPosition.setScale(0, RoundingMode.FLOOR).intValueExact();
        int upperIndex = zeroBasedPosition.setScale(0, RoundingMode.CEILING).intValueExact();
        if (lowerIndex == upperIndex) {
            return scaledValueAt(sortedSalaries, lowerIndex);
        }

        BigDecimal fraction = zeroBasedPosition.subtract(BigDecimal.valueOf(lowerIndex));
        BigDecimal lowerValue = sortedSalaries.get(lowerIndex);
        BigDecimal interpolatedValue = lowerValue.add(
                sortedSalaries.get(upperIndex)
                        .subtract(lowerValue)
                        .multiply(fraction)
        );
        return BigDecimalUtil.scale(SalaryUtil.SALARY_SCALE, interpolatedValue);
    }

    private static BigDecimal scaledValueAt(List<BigDecimal> sortedSalaries, int index) {
        return BigDecimalUtil.scale(SalaryUtil.SALARY_SCALE, sortedSalaries.get(index));
    }

    private static boolean hasOddSize(List<BigDecimal> salaries) {
        return salaries.size() % MEDIAN_PAIR_SIZE != 0;
    }
}
