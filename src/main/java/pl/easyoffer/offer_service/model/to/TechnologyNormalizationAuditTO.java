package pl.easyoffer.offer_service.model.to;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TechnologyNormalizationAuditTO {

    private String normalizationVersion;
    private long technologyValueCount;
    private long changedTechnologyValueCount;
    private long offerTechnologyAssociationCount;
    private long changedOfferTechnologyAssociationCount;
}
