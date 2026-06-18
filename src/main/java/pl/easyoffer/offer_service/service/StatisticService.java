package pl.easyoffer.offer_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pl.easyoffer.offer_service.model.OfferSearchRequest;
import pl.easyoffer.offer_service.model.to.CategoryStatisticTO;
import pl.easyoffer.offer_service.model.to.OfferResponseTO;
import pl.easyoffer.offer_service.model.to.StatisticTO;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticService {

    private static final int STATISTICS_SIZE_LIMIT = 10;

    private final OfferService offerService;

    public StatisticTO getStatistics() {
        return StatisticTO.builder()
                .categoriesStatistic(retrieveCategoriesStatistic())
                .newestJobOffers(retrieveNewestOffers())
                .build();
    }

    private List<CategoryStatisticTO> retrieveCategoriesStatistic() {
        Pageable pageable = PageRequest.of(0, STATISTICS_SIZE_LIMIT);
        return offerService.getCategoryStatistics(pageable);
    }

    private List<OfferResponseTO> retrieveNewestOffers() {
        OfferSearchRequest searchRequest =  OfferSearchRequest.builder()
                .updatedAtFrom(LocalDateTime.now().toLocalDate().atStartOfDay())
                .updatedAtTo(LocalDateTime.now())
                .build();
        Pageable pageable = PageRequest.of(0, STATISTICS_SIZE_LIMIT);
        return offerService.search(searchRequest, pageable).getContent();
    }

}
