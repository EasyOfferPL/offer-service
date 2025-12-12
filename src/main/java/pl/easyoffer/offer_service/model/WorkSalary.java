package pl.easyoffer.offer_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldNameConstants;

import java.math.BigDecimal;

@Builder
@Data
@AllArgsConstructor
@EqualsAndHashCode
@FieldNameConstants
public class WorkSalary {

    private BigDecimal min;
    private BigDecimal max;
    private String currency;

}
