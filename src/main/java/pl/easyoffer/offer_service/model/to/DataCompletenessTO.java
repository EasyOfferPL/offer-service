package pl.easyoffer.offer_service.model.to;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class DataCompletenessTO {

    private String completenessVersion;
    private String salaryNormalizationVersion;
    private String periodField;
    private String unitOfAnalysis;
    private List<String> sources;
    private LocalDateTime dateFrom;
    private LocalDateTime dateTo;
    private long eligibleOfferCount;
    private List<DataCompletenessSourceTO> bySource;

}
