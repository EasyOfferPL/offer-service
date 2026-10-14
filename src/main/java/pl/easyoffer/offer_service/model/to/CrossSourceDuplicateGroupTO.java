package pl.easyoffer.offer_service.model.to;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CrossSourceDuplicateGroupTO {

    private String deduplicationFingerprint;
    private List<DeduplicationAuditOfferTO> offers;

}
