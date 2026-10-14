package pl.easyoffer.offer_service.model.to;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DeduplicationAuditTO {

    private long rawOfferCount;
    private long uniqueOfferCount;
    private long crossSourceDuplicateGroupCount;
    private int groupPage;
    private int groupSize;
    private long totalGroupPages;
    private List<CrossSourceDuplicateGroupTO> groups;

}
