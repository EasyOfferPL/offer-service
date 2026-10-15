package pl.easyoffer.offer_service.model.to;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class SalaryComparabilityAuditTO {

    private String normalizationVersion;
    private long rawOfferCount;
    private long salaryPresentOfferCount;
    private long usableOfferCount;
    private long excludedOfferCount;
    private long usableSingleBoundaryOfferCount;
    private Map<String, Long> exclusionCounts;
}
