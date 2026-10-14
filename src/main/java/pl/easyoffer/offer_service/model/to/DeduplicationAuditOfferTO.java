package pl.easyoffer.offer_service.model.to;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeduplicationAuditOfferTO {

    private Long offerId;
    private String source;
    private String externalId;

}
