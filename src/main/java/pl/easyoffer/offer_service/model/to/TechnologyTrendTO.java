package pl.easyoffer.offer_service.model.to;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TechnologyTrendTO {

    private String month;
    private Long count;

}
