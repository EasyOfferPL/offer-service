package pl.easyoffer.offer_service.model.to;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class SalaryTrendPointTO {

    private String month;
    private BigDecimal averageSalary;
    private BigDecimal medianSalary;
    private long offersCount;

}
