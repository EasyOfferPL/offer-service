package pl.easyoffer.offer_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pl.easyoffer.offer_service.model.to.CategoryStatisticTO;
import pl.easyoffer.offer_service.model.to.OfferResponseTO;
import pl.easyoffer.offer_service.model.to.StatisticTO;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class StatisticService {

    private static final int OFFERS_STATISTICS_LIMIT = 10;

    private final OfferService offerService;

    public StatisticTO getStatistics() {
        return StatisticTO.builder()
                .categoriesStatistic(retrieveCategoriesStatistic())
                .newestJobOffers(retrieveNewestOffers())
                .build();
    }

    private List<CategoryStatisticTO> retrieveCategoriesStatistic() {
        Pageable pageable = PageRequest.of(0, OFFERS_STATISTICS_LIMIT);
        return offerService.getCategoryStatistics(pageable);
    }

    private Set<OfferResponseTO> retrieveNewestOffers() {
        return null;
    }

}
