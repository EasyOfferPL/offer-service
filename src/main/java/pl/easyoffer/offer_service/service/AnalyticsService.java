package pl.easyoffer.offer_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pl.easyoffer.offer_service.exception.ValidationException;
import pl.easyoffer.offer_service.model.OfferSearchRequest;
import pl.easyoffer.offer_service.model.SalaryType;
import pl.easyoffer.offer_service.model.SalaryUnit;
import pl.easyoffer.offer_service.model.entity.OfferEntity;
import pl.easyoffer.offer_service.model.to.*;
import pl.easyoffer.offer_service.repository.projection.DeduplicationAuditOfferProjection;
import pl.easyoffer.offer_service.service.calculator.SalaryAnalyticsCalculator;
import pl.easyoffer.offer_service.service.calculator.SalaryComparabilityAuditCalculator;
import pl.easyoffer.offer_service.service.calculator.TechnologyPopularityCalculation;
import pl.easyoffer.offer_service.service.calculator.TechnologyPopularityCalculator;
import pl.easyoffer.offer_service.service.deduplication.CrossSourceFingerprintV1;
import pl.easyoffer.offer_service.service.normalization.SalaryComparabilityInput;
import pl.easyoffer.offer_service.service.normalization.SalaryComparabilityPolicy;
import pl.easyoffer.offer_service.service.normalization.TechnologyNormalizer;
import pl.easyoffer.offer_service.service.persistence.OfferPersistenceService;
import pl.easyoffer.offer_service.service.persistence.TechnologyPersistenceService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.util.StringUtils.hasText;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private static final int SHORT_ANALYTICS_PAGE_SIZE_LIMIT = 10;
    private static final int TECHNOLOGY_SHARE_SCALE = 4;
    private static final String TECHNOLOGY_POPULARITY_PERIOD_FIELD = "PUBLISHED_AT";

    private final OfferService offerService;
    private final OfferPersistenceService offerPersistenceService;
    private final TechnologyPersistenceService technologyPersistenceService;
    private final SalaryAnalyticsCalculator salaryAnalyticsCalculator;
    private final SalaryComparabilityAuditCalculator salaryComparabilityAuditCalculator;
    private final TechnologyNormalizer technologyNormalizer;
    private final TechnologyPopularityCalculator technologyPopularityCalculator;

    public List<OfferResponseTO> retrieveNewestOffers() {
        OfferSearchRequest searchRequest = OfferSearchRequest.builder()
                .updatedAtFrom(LocalDateTime.now().toLocalDate().atStartOfDay())
                .updatedAtTo(LocalDateTime.now())
                .build();
        Pageable pageable = PageRequest.of(0, SHORT_ANALYTICS_PAGE_SIZE_LIMIT);
        return offerService.search(searchRequest, pageable).getContent();
    }

    public List<TechnologiesAnalyticsTO> getTechnologiesAnalytics(
            String categoryName,
            LocalDateTime from,
            LocalDateTime to
    ) {
        TechnologyPopularityAnalysis analysis = analyzeTechnologyPopularity(
                categoryName, List.of(), from, to
        );
        return toTechnologyPopularityItems(analysis.calculation()).stream()
                .map(technology -> TechnologiesAnalyticsTO.builder()
                        .technologyName(technology.getTechnologyName())
                        .count(technology.getCount())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public TechnologyPopularityTO getTechnologyPopularity(
            String categoryName,
            List<String> sources,
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            int page,
            int size
    ) {
        validatePagination(page, size);
        TechnologyPopularityAnalysis analysis = analyzeTechnologyPopularity(
                categoryName, sources, dateFrom, dateTo
        );
        TechnologyPopularityCalculation calculation = analysis.calculation();
        List<TechnologyPopularityItemTO> allTechnologies = toTechnologyPopularityItems(calculation);
        List<TechnologyPopularityItemTO> technologyPage = page(allTechnologies, page, size);

        return TechnologyPopularityTO.builder()
                .technologyNormalizationVersion(technologyNormalizer.version())
                .deduplicationVersion(CrossSourceFingerprintV1.version())
                .periodField(TECHNOLOGY_POPULARITY_PERIOD_FIELD)
                .categoryName(analysis.categoryName())
                .sources(analysis.sources())
                .dateFrom(dateFrom)
                .dateTo(dateTo)
                .rawOfferCount(calculation.rawOfferCount())
                .uniqueOfferCount(calculation.uniqueOfferCount())
                .eligibleOfferCount(calculation.eligibleOfferCount())
                .page(page)
                .size(size)
                .totalElements(allTechnologies.size())
                .totalPages(Math.ceilDiv(allTechnologies.size(), (long) size))
                .technologies(technologyPage)
                .build();
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
    public SalaryComparabilityAuditTO getSalaryComparabilityAudit() {
        try (Stream<SalaryComparabilityInput> inputs =
                     offerPersistenceService.streamSalaryComparabilityInputs()) {
            return salaryComparabilityAuditCalculator.calculate(inputs);
        }
    }

    @Transactional(readOnly = true)
    public SalaryTrendTO getSalaryTrend(
            String categoryName,
            String currency,
            String employmentType,
            SalaryType salaryType,
            String experienceLevel,
            LocalDateTime dateFrom,
            LocalDateTime dateTo
    ) {
        validateDateRange(dateFrom, dateTo);
        if (!SalaryComparabilityPolicy.COMPARABLE_CURRENCY.equalsIgnoreCase(currency)) {
            throw new ValidationException("Only PLN salaries are comparable");
        }
        if (!hasText(employmentType)) {
            throw new ValidationException("employmentType must not be blank");
        }
        if (salaryType == null || salaryType == SalaryType.UNKNOWN) {
            throw new ValidationException("salaryType must be GROSS or NET");
        }

        String normalizedEmploymentType = employmentType.trim().toUpperCase(Locale.ROOT);

        List<OfferEntity> offers = offerPersistenceService.findForSalaryTrend(
                categoryName,
                SalaryComparabilityPolicy.COMPARABLE_CURRENCY,
                normalizedEmploymentType,
                salaryType,
                experienceLevel,
                dateFrom,
                dateTo
        );
        return SalaryTrendTO.builder()
                .categoryName(categoryName)
                .currency(SalaryComparabilityPolicy.COMPARABLE_CURRENCY)
                .employmentType(normalizedEmploymentType)
                .experienceLevel(experienceLevel)
                .salaryUnit(SalaryUnit.MONTH.name())
                .salaryType(salaryType)
                .points(salaryAnalyticsCalculator.calculateMonthlyTrend(offers, dateFrom, dateTo))
                .build();
    }

    private LocalDateTime getPublicationDate(OfferResponseTO offer) {
        return Objects.nonNull(offer.getPublishedAt()) ? offer.getPublishedAt() : offer.getCreatedAt();
    }

    private List<TechnologyPopularityItemTO> toTechnologyPopularityItems(
            TechnologyPopularityCalculation calculation
    ) {
        return calculation.offerCountsByTechnology().entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed()
                        .thenComparing(Map.Entry::getKey, String.CASE_INSENSITIVE_ORDER))
                .map(entry -> TechnologyPopularityItemTO.builder()
                        .technologyName(entry.getKey())
                        .count(entry.getValue())
                        .denominator(calculation.eligibleOfferCount())
                        .sharePercentage(calculateSharePercentage(
                                entry.getValue(), calculation.eligibleOfferCount()
                        ))
                        .build())
                .toList();
    }

    private TechnologyPopularityAnalysis analyzeTechnologyPopularity(
            String categoryName,
            List<String> sources,
            LocalDateTime dateFrom,
            LocalDateTime dateTo
    ) {
        validateDateRange(dateFrom, dateTo);
        String normalizedCategoryName = hasText(categoryName) ? categoryName.trim() : null;
        List<String> normalizedSources = normalizeSources(sources);
        List<OfferEntity> offers = offerPersistenceService.findForTechnologyPopularity(
                normalizedCategoryName, normalizedSources, dateFrom, dateTo
        );
        List<String> responseSources = normalizedSources.isEmpty()
                ? sourcesPresentIn(offers)
                : normalizedSources;
        return new TechnologyPopularityAnalysis(
                normalizedCategoryName,
                responseSources,
                technologyPopularityCalculator.calculate(offers)
        );
    }

    private List<TechnologyPopularityItemTO> page(
            List<TechnologyPopularityItemTO> technologies,
            int page,
            int size
    ) {
        long requestedOffset = (long) page * size;
        if (requestedOffset >= technologies.size()) {
            return List.of();
        }
        int fromIndex = Math.toIntExact(requestedOffset);
        int toIndex = Math.toIntExact(Math.min(requestedOffset + size, technologies.size()));
        return technologies.subList(fromIndex, toIndex);
    }

    private BigDecimal calculateSharePercentage(long count, long denominator) {
        if (denominator == 0) {
            return BigDecimal.ZERO.setScale(TECHNOLOGY_SHARE_SCALE);
        }
        return BigDecimal.valueOf(count)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(denominator), TECHNOLOGY_SHARE_SCALE, RoundingMode.HALF_UP);
    }

    private List<String> normalizeSources(List<String> sources) {
        if (sources == null || sources.isEmpty()) {
            return List.of();
        }
        if (sources.stream().anyMatch(source -> !hasText(source))) {
            throw new ValidationException("sources must not contain blank values");
        }
        return sources.stream()
                .map(source -> source.trim().toUpperCase(Locale.ROOT))
                .distinct()
                .sorted()
                .toList();
    }

    private List<String> sourcesPresentIn(List<OfferEntity> offers) {
        return offers.stream()
                .map(OfferEntity::getSource)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList();
    }

    private void validateDateRange(LocalDateTime dateFrom, LocalDateTime dateTo) {
        if (dateFrom == null || dateTo == null) {
            throw new ValidationException("dateFrom and dateTo are required");
        }
        if (dateFrom.isAfter(dateTo)) {
            throw new ValidationException("dateFrom must not be after dateTo");
        }
    }

    private void validatePagination(int page, int size) {
        if (page < 0) {
            throw new ValidationException("page must not be negative");
        }
        if (size < 1) {
            throw new ValidationException("size must be positive");
        }
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

    private record TechnologyPopularityAnalysis(
            String categoryName,
            List<String> sources,
            TechnologyPopularityCalculation calculation
    ) {
    }

}
