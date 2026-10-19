package pl.easyoffer.offer_service.model.to;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SalaryAnalyticsTO {

    private String salaryNormalizationVersion;
    private String quantileMethod;
    private int recommendedMinimumSampleSize;
    private List<SalaryStatisticTO> byCategory;
    private List<SalaryStatisticTO> byExperienceLevel;

}
