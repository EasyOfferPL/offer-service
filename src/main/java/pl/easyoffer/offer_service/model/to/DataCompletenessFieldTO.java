package pl.easyoffer.offer_service.model.to;

import lombok.Builder;
import lombok.Data;
import pl.easyoffer.offer_service.model.DataCompletenessField;

import java.math.BigDecimal;

@Data
@Builder
public class DataCompletenessFieldTO {

    private DataCompletenessField field;
    private long usableOfferCount;
    private long unusableOfferCount;
    private long eligibleOfferCount;
    private BigDecimal completenessPercentage;

}
