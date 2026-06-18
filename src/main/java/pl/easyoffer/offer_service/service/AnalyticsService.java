package pl.easyoffer.offer_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.easyoffer.offer_service.model.OfferSearchRequest;
import pl.easyoffer.offer_service.model.to.CategoryAnalyticsTO;
import pl.easyoffer.offer_service.model.to.OfferResponseTO;
import pl.easyoffer.offer_service.model.to.TechnologiesAnalyticsTO;
import pl.easyoffer.offer_service.service.persistence.OfferPersistenceService;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private static final int SHORT_ANALYTICS_PAGE_SIZE_LIMIT = 10;

    private final OfferService offerService;
    private final OfferPersistenceService offerPersistenceService;

    public List<OfferResponseTO> retrieveNewestOffers() {
        OfferSearchRequest searchRequest = OfferSearchRequest.builder()
                .updatedAtFrom(LocalDateTime.now().toLocalDate().atStartOfDay())
                .updatedAtTo(LocalDateTime.now())
                .build();
        Pageable pageable = PageRequest.of(0, SHORT_ANALYTICS_PAGE_SIZE_LIMIT);
        return offerService.search(searchRequest, pageable).getContent();
    }

    public List<TechnologiesAnalyticsTO> getTechnologiesAnalytics(String categoryName, LocalDateTime from, LocalDateTime to) {
        OfferSearchRequest searchRequest = OfferSearchRequest.builder()
                .categoryNames(List.of(categoryName))
                .updatedAtFrom(from)
                .updatedAtTo(to)
                .build();
        return offerService.search(searchRequest).stream()
                .map(OfferResponseTO::getTechnologies)
                .flatMap(Collection::stream)
                .collect(Collectors.groupingBy(
                        Function.identity(),
                        Collectors.counting()
                )).entrySet()
                .stream()
                .map(entry -> TechnologiesAnalyticsTO.builder()
                        .technologyName(entry.getKey())
                        .count(entry.getValue())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CategoryAnalyticsTO> getCategoryAnalytics() {
        Pageable pageable = PageRequest.of(0, SHORT_ANALYTICS_PAGE_SIZE_LIMIT);
        return Optional.ofNullable(offerPersistenceService.getCategoryStatistics(pageable))
                .orElseGet(List::of)
                .stream()
                .map(rawCategoryStatistic -> CategoryAnalyticsTO.builder()
                        .categoryName(rawCategoryStatistic.getCategoryName())
                        .offersCount(rawCategoryStatistic.getOfferCount())
                        .build())
                .toList();
    }

}
