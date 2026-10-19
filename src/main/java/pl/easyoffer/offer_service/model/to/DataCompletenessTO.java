package pl.easyoffer.offer_service.model.to;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class DataCompletenessTO {

    private String completenessVersion;
    private String salaryNormalizationVersion;
    private String periodField;
    private String unitOfAnalysis;
    private List<String> sources;
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private long eligibleOfferCount;
    private List<DataCompletenessSourceTO> bySource;

}
