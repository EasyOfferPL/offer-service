package pl.easyoffer.offer_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pl.easyoffer.offer_service.exception.ValidationException;
import pl.easyoffer.offer_service.model.OfferSearchRequest;
import pl.easyoffer.offer_service.model.SalaryUnit;
import pl.easyoffer.offer_service.model.entity.OfferEntity;
import pl.easyoffer.offer_service.model.to.*;
import pl.easyoffer.offer_service.repository.projection.DeduplicationAuditOfferProjection;
import pl.easyoffer.offer_service.service.calculator.SalaryAnalyticsCalculator;
import pl.easyoffer.offer_service.service.normalization.TechnologyNormalizer;
import pl.easyoffer.offer_service.service.persistence.OfferPersistenceService;
import pl.easyoffer.offer_service.service.persistence.TechnologyPersistenceService;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private static final int SHORT_ANALYTICS_PAGE_SIZE_LIMIT = 10;

    private final OfferService offerService;
    private final OfferPersistenceService offerPersistenceService;
    private final TechnologyPersistenceService technologyPersistenceService;
    private final SalaryAnalyticsCalculator salaryAnalyticsCalculator;
    private final TechnologyNormalizer technologyNormalizer;

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

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public DeduplicationAuditTO getDeduplicationAudit(int groupPage, int groupSize) {
        long rawOfferCount = offerPersistenceService.countOffers();
        long crossSourceDuplicateGroupCount = offerPersistenceService.countCrossSourceDuplicateGroups();
        long groupedOfferCount = offerPersistenceService.countCrossSourceDuplicateGroupMembers();
        List<String> groupFingerprints = offerPersistenceService.findCrossSourceDuplicateFingerprints(
                PageRequest.of(groupPage, groupSize)
        );
        Map<String, List<DeduplicationAuditOfferProjection>> groupedOffers = offerPersistenceService
                .findCrossSourceDuplicateGroupMembers(groupFingerprints)
                .stream()
                .collect(Collectors.groupingBy(
                        DeduplicationAuditOfferProjection::getDeduplicationFingerprint,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<CrossSourceDuplicateGroupTO> groups = groupedOffers.entrySet()
                .stream()
                .map(entry -> CrossSourceDuplicateGroupTO.builder()
                        .deduplicationFingerprint(entry.getKey())
                        .offers(entry.getValue().stream()
                                .map(this::toDeduplicationAuditOffer)
                                .toList())
                        .build())
                .toList();

        return DeduplicationAuditTO.builder()
                .rawOfferCount(rawOfferCount)
                .uniqueOfferCount(rawOfferCount - groupedOfferCount + crossSourceDuplicateGroupCount)
                .crossSourceDuplicateGroupCount(crossSourceDuplicateGroupCount)
                .groupPage(groupPage)
                .groupSize(groupSize)
                .totalGroupPages(Math.ceilDiv(crossSourceDuplicateGroupCount, (long) groupSize))
                .groups(groups)
                .build();
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public TechnologyNormalizationAuditTO getTechnologyNormalizationAudit() {
        String normalizationVersion = technologyNormalizer.version();
        return TechnologyNormalizationAuditTO.builder()
                .normalizationVersion(normalizationVersion)
                .technologyValueCount(technologyPersistenceService.countValues(normalizationVersion))
                .changedTechnologyValueCount(
                        technologyPersistenceService.countChangedValues(normalizationVersion)
                )
                .offerTechnologyAssociationCount(
                        technologyPersistenceService.countOfferAssociations(normalizationVersion)
                )
                .changedOfferTechnologyAssociationCount(
                        technologyPersistenceService.countChangedOfferAssociations(normalizationVersion)
                )
                .build();
    }

    public List<TechnologyTrendTO> getTechnologyTrend(
            String categoryName,
            LocalDateTime from,
            LocalDateTime to
    ) {
        OfferSearchRequest searchRequest = OfferSearchRequest.builder()
                .categoryNames(List.of(categoryName))
                .build();

        return offerService.search(searchRequest).stream()
                .filter(offer -> {
                    LocalDateTime date = getPublicationDate(offer);
                    return !date.isBefore(from) && !date.isAfter(to);
                })
                .collect(Collectors.groupingBy(
                        offer -> YearMonth.from(getPublicationDate(offer)),
                        TreeMap::new,
                        Collectors.counting()
                ))
                .entrySet()
                .stream()
                .map(entry -> TechnologyTrendTO.builder()
                        .month(entry.getKey().toString())
                        .count(entry.getValue())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public SalaryAnalyticsTO getSalaryAnalytics(String categoryName, String experienceLevel) {
        List<OfferEntity> offers = offerPersistenceService.findForSalaryAnalytics(categoryName, experienceLevel);

        return SalaryAnalyticsTO.builder()
                .byCategory(salaryAnalyticsCalculator.calculateByCategory(offers))
                .byExperienceLevel(salaryAnalyticsCalculator.calculateByExperienceLevel(offers))
                .build();
    }

    @Transactional(readOnly = true)
    public SalaryTrendTO getSalaryTrend(
            String categoryName,
            String currency,
            String employmentType,
            String experienceLevel,
            LocalDateTime dateFrom,
            LocalDateTime dateTo
    ) {
        if (dateFrom.isAfter(dateTo)) {
            throw new ValidationException("dateFrom must not be after dateTo");
        }

        List<OfferEntity> offers = offerPersistenceService.findForSalaryTrend(
                categoryName, currency, employmentType, experienceLevel, dateFrom, dateTo
        );
        return SalaryTrendTO.builder()
                .categoryName(categoryName)
                .currency(currency)
                .employmentType(employmentType)
                .experienceLevel(experienceLevel)
                .salaryUnit(SalaryUnit.MONTH.name())
                .points(salaryAnalyticsCalculator.calculateMonthlyTrend(offers, dateFrom, dateTo))
                .build();
    }

    private LocalDateTime getPublicationDate(OfferResponseTO offer) {
        return Objects.nonNull(offer.getPublishedAt()) ? offer.getPublishedAt() : offer.getCreatedAt();
    }

    private DeduplicationAuditOfferTO toDeduplicationAuditOffer(
            DeduplicationAuditOfferProjection offer
    ) {
        return DeduplicationAuditOfferTO.builder()
                .offerId(offer.getOfferId())
                .source(offer.getSource())
                .externalId(offer.getExternalId())
                .build();
    }

}
