package pl.easyoffer.offer_service.model.to;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class SalaryStatisticTO {

    private String groupValue;
    private String currency;
    private String salaryUnit;
    private String employmentType;
    private BigDecimal averageSalary;
    private BigDecimal medianSalary;
    private long offersCount;

}
