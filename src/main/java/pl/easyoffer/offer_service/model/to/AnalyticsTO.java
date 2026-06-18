package pl.easyoffer.offer_service.model.to;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AnalyticsTO {

    private List<CategoryAnalyticsTO> categoriesStatistic;
    private List<OfferResponseTO> newestJobOffers;

}
