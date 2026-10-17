package pl.easyoffer.offer_service.model.to;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class TechnologyTrendTO {

    private String technologyNormalizationVersion;
    private String deduplicationVersion;
    private String periodField;
    private String monthAssignment;
    private String technologyName;
    private String categoryName;
    private List<String> sources;
    private LocalDateTime dateFrom;
    private LocalDateTime dateTo;
    private long rawOfferCount;
    private long uniqueOfferCount;
    private long eligibleOfferCount;
    private List<TechnologyTrendPointTO> points;

}
