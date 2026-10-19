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
import pl.easyoffer.offer_service.service.calculator.*;
import pl.easyoffer.offer_service.service.deduplication.CrossSourceFingerprintV1;
import pl.easyoffer.offer_service.service.normalization.SalaryComparabilityInput;
import pl.easyoffer.offer_service.service.normalization.SalaryComparabilityPolicy;
import pl.easyoffer.offer_service.service.normalization.TechnologyNormalizationResult;
import pl.easyoffer.offer_service.service.normalization.TechnologyNormalizer;
import pl.easyoffer.offer_service.service.persistence.OfferPersistenceService;
import pl.easyoffer.offer_service.service.persistence.TechnologyPersistenceService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
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
    private static final String TECHNOLOGY_TREND_MONTH_ASSIGNMENT = "EARLIEST_PUBLISHED_AT";
    private static final long MAX_TECHNOLOGY_TREND_MONTHS = 120;

    private final OfferService offerService;
    private final OfferPersistenceService offerPersistenceService;
    private final TechnologyPersistenceService technologyPersistenceService;
    private final SalaryAnalyticsCalculator salaryAnalyticsCalculator;
    private final SalaryComparabilityAuditCalculator salaryComparabilityAuditCalculator;
    private final DataCompletenessCalculator dataCompletenessCalculator;
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

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public TechnologyTrendTO getTechnologyTrend(
            String technologyName,
            String categoryName,
            List<String> sources,
            LocalDateTime dateFrom,
            LocalDateTime dateTo
    ) {
        validateDateRange(dateFrom, dateTo);
        YearMonth monthFrom = YearMonth.from(dateFrom);
        YearMonth monthTo = YearMonth.from(dateTo);
        validateTechnologyTrendRange(monthFrom, monthTo);
        String canonicalTechnologyName = technologyNormalizer.normalize(technologyName)
                .map(TechnologyNormalizationResult::canonicalName)
                .orElseThrow(() -> new ValidationException("technologyName must not be blank"));
        TechnologyOfferScope scope = loadTechnologyOffers(
                categoryName, sources, dateFrom, dateTo
        );
        TechnologyTrendCalculation calculation = technologyPopularityCalculator.calculateMonthlyTrend(
                scope.offers(),
                canonicalTechnologyName,
                monthFrom,
                monthTo
        );

        return TechnologyTrendTO.builder()
                .technologyNormalizationVersion(technologyNormalizer.version())
                .deduplicationVersion(CrossSourceFingerprintV1.version())
                .periodField(TECHNOLOGY_POPULARITY_PERIOD_FIELD)
                .monthAssignment(TECHNOLOGY_TREND_MONTH_ASSIGNMENT)
                .technologyName(canonicalTechnologyName)
                .categoryName(scope.categoryName())
                .sources(scope.sources())
                .dateFrom(dateFrom)
                .dateTo(dateTo)
                .rawOfferCount(calculation.rawOfferCount())
                .uniqueOfferCount(calculation.uniqueOfferCount())
                .eligibleOfferCount(calculation.eligibleOfferCount())
                .points(calculation.points().stream()
                        .map(point -> TechnologyTrendPointTO.builder()
                                .month(point.month().toString())
                                .count(point.count())
                                .denominator(point.denominator())
                                .sharePercentage(calculateSharePercentage(
                                        point.count(), point.denominator()
                                ))
                                .build())
                        .toList())
                .build();
    }

    @Transactional(readOnly = true)
    public SalaryAnalyticsTO getSalaryAnalytics(String categoryName, String experienceLevel) {
        List<OfferEntity> offers = offerPersistenceService.findForSalaryAnalytics(categoryName, experienceLevel);

        return SalaryAnalyticsTO.builder()
                .salaryNormalizationVersion(salaryAnalyticsCalculator.normalizationVersion())
                .quantileMethod(salaryAnalyticsCalculator.quantileMethod())
                .recommendedMinimumSampleSize(
                        salaryAnalyticsCalculator.recommendedMinimumSampleSize()
                )
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
    public DataCompletenessTO getDataCompleteness(
            List<String> sources,
            LocalDateTime dateFrom,
            LocalDateTime dateTo
    ) {
        validateDateRange(dateFrom, dateTo);
        List<String> normalizedSources = normalizeSources(sources);
        DataCompletenessCalculation calculation;
        try (Stream<DataCompletenessInput> inputs = offerPersistenceService
                .streamDataCompletenessInputs(normalizedSources, dateFrom, dateTo)) {
            calculation = dataCompletenessCalculator.calculate(inputs);
        }

        List<String> responseSources = normalizedSources.isEmpty()
                ? calculation.bySource().stream()
                        .map(DataCompletenessSourceTO::getSource)
                        .toList()
                : normalizedSources;

        return DataCompletenessTO.builder()
                .completenessVersion(DataCompletenessCalculator.VERSION)
                .salaryNormalizationVersion(dataCompletenessCalculator.salaryNormalizationVersion())
                .periodField(DataCompletenessCalculator.PERIOD_FIELD)
                .unitOfAnalysis(DataCompletenessCalculator.UNIT_OF_ANALYSIS)
                .sources(responseSources)
                .dateFrom(dateFrom)
                .dateTo(dateTo)
                .eligibleOfferCount(calculation.eligibleOfferCount())
                .bySource(calculation.bySource())
                .build();
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
        TechnologyOfferScope scope = loadTechnologyOffers(
                categoryName, sources, dateFrom, dateTo
        );
        return new TechnologyPopularityAnalysis(
                scope.categoryName(),
                scope.sources(),
                technologyPopularityCalculator.calculate(scope.offers())
        );
    }

    private TechnologyOfferScope loadTechnologyOffers(
            String categoryName,
            List<String> sources,
            LocalDateTime dateFrom,
            LocalDateTime dateTo
    ) {
        String normalizedCategoryName = hasText(categoryName) ? categoryName.trim() : null;
        List<String> normalizedSources = normalizeSources(sources);
        List<OfferEntity> offers = offerPersistenceService.findForTechnologyAnalytics(
                normalizedCategoryName, normalizedSources, dateFrom, dateTo
        );
        List<String> responseSources = normalizedSources.isEmpty()
                ? sourcesPresentIn(offers)
                : normalizedSources;
        return new TechnologyOfferScope(normalizedCategoryName, responseSources, offers);
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

    private void validateTechnologyTrendRange(YearMonth monthFrom, YearMonth monthTo) {
        long requestedMonths = ChronoUnit.MONTHS.between(monthFrom, monthTo) + 1;
        if (requestedMonths > MAX_TECHNOLOGY_TREND_MONTHS) {
            throw new ValidationException(
                    "Technology trend range must not exceed "
                            + MAX_TECHNOLOGY_TREND_MONTHS + " months"
            );
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

    private record TechnologyOfferScope(
            String categoryName,
            List<String> sources,
            List<OfferEntity> offers
    ) {
    }

}
