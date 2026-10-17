package pl.easyoffer.offer_service.model.to;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class TechnologyTrendPointTO {

    private String month;
    private long count;
    private long denominator;
    private BigDecimal sharePercentage;

}
