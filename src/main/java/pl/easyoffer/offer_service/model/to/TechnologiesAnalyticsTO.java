package pl.easyoffer.offer_service.model.to;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TechnologiesAnalyticsTO {

    private String technologyName;
    private Long count;

}
