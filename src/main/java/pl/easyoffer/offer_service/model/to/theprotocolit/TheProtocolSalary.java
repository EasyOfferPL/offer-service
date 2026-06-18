package pl.easyoffer.offer_service.model.to.theprotocolit;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TheProtocolSalary {

    private BigDecimal from;
    private BigDecimal to;
    private String currency;
    private String currencySymbol;
    private TheProtocolTimeUnit timeUnit;
    private String kindName;

}
