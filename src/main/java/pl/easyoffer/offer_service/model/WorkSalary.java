package pl.easyoffer.offer_service.model;

import lombok.*;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;
import java.math.BigDecimal;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@FieldNameConstants
public class WorkSalary implements Serializable {

    private BigDecimal min;
    private BigDecimal max;
    private String currency;

}
