package pl.easyoffer.offer_service.model.to;

import lombok.Builder;
import lombok.Data;
import pl.easyoffer.offer_service.model.SalaryType;

import java.math.BigDecimal;

@Data
@Builder
public class SalaryStatisticTO {

    private String groupValue;
    private String currency;
    private String salaryUnit;
    private String employmentType;
    private SalaryType salaryType;
    private BigDecimal minimumSalary;
    private BigDecimal percentile25Salary;
    private BigDecimal averageSalary;
    private BigDecimal medianSalary;
    private BigDecimal percentile75Salary;
    private BigDecimal maximumSalary;
    private long offersCount;
    private boolean meetsRecommendedSampleSize;

}
