package pl.easyoffer.offer_service.model.to;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
@Builder
public class StatisticTO {

    private List<CategoryStatisticTO> categoriesStatistic;
    private Set<OfferResponseTO> newestJobOffers;

}
