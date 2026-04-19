package pl.easyoffer.offer_service.model.dto.justjoinit;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class JustJoinItEmploymentType {

    private BigDecimal from;
    private BigDecimal fromPerUnit;
    private BigDecimal to;
    private BigDecimal toPerUnit;
    private String currency;
    private String currencySource;
    private String type;
    private String unit;
    private Boolean gross;

}
