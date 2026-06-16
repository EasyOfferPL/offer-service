package pl.easyoffer.offer_service.model.to;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryStatisticTO {

    private String categoryName;
    private Long offersCount;

}
