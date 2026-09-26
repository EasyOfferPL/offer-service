package pl.easyoffer.offer_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.easyoffer.offer_service.exception.ValidationException;
import pl.easyoffer.offer_service.model.entity.OfferEntity;
import pl.easyoffer.offer_service.model.entity.TechnologyEntity;
import pl.easyoffer.offer_service.model.to.SalaryAnalyticsTO;
import pl.easyoffer.offer_service.model.to.SalaryStatisticTO;
import pl.easyoffer.offer_service.model.to.SalaryTrendTO;
import pl.easyoffer.offer_service.service.calculator.SalaryAnalyticsCalculator;
import pl.easyoffer.offer_service.service.persistence.OfferPersistenceService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private OfferService offerService;

    @Mock
    private OfferPersistenceService offerPersistenceService;

    private AnalyticsService analyticsService;

    @BeforeEach
    void setUp() {
        analyticsService = new AnalyticsService(offerService, offerPersistenceService, new SalaryAnalyticsCalculator());
    }

    @Test
    void shouldCalculateAverageAndOddMedianFromSalaryRanges() {
        when(offerPersistenceService.findForSalaryAnalytics(null, null)).thenReturn(List.of(
                offer("Java", "Mid", "10000", "14000"),
                offer("Java", "Mid", "14000", "18000"),
                offer("Java", "Mid", "18000", "22000")
        ));

        SalaryStatisticTO result = analyticsService.getSalaryAnalytics(null, null).getByCategory().getFirst();

        assertThat(result.getAverageSalary()).isEqualByComparingTo("16000.00");
        assertThat(result.getMedianSalary()).isEqualByComparingTo("16000.00");
        assertThat(result.getOffersCount()).isEqualTo(3);
    }

    @Test
    void shouldCalculateEvenMedianAndUseAvailableSalaryBoundary() {
        when(offerPersistenceService.findForSalaryAnalytics(null, null)).thenReturn(List.of(
                offer("Java", "Senior", "10000", null),
                offer("Java", "Senior", null, "14000")
        ));

        SalaryStatisticTO result = analyticsService.getSalaryAnalytics(null, null).getByExperienceLevel().getFirst();

        assertThat(result.getAverageSalary()).isEqualByComparingTo("12000.00");
        assertThat(result.getMedianSalary()).isEqualByComparingTo("12000.00");
        assertThat(result.getOffersCount()).isEqualTo(2);
    }

    @Test
    void shouldPassCategoryAndExperienceFilters() {
        when(offerPersistenceService.findForSalaryAnalytics("Backend", "Senior")).thenReturn(List.of(
                offer("Backend", "Senior", "10000", "14000")
        ));

        SalaryAnalyticsTO result = analyticsService.getSalaryAnalytics("Backend", "Senior");

        verify(offerPersistenceService).findForSalaryAnalytics("Backend", "Senior");
        assertThat(result.getByCategory()).extracting(SalaryStatisticTO::getGroupValue)
                .containsExactly("Backend");
        assertThat(result.getByExperienceLevel()).extracting(SalaryStatisticTO::getGroupValue)
                .containsExactly("Senior");
    }

    @Test
    void shouldIgnoreOffersWithoutSalaryAndReturnEmptyListsWhenNoUsableDataExists() {
        when(offerPersistenceService.findForSalaryAnalytics(null, null)).thenReturn(List.of(
                offer("Java", "Junior", null, null)
        ));

        SalaryAnalyticsTO result = analyticsService.getSalaryAnalytics(null, null);

        assertThat(result.getByCategory()).isEmpty();
        assertThat(result.getByExperienceLevel()).isEmpty();
    }

    @Test
    void shouldReturnEmptyListsWhenNoOffersMatchFilters() {
        when(offerPersistenceService.findForSalaryAnalytics("Rust", "Junior")).thenReturn(List.of());

        SalaryAnalyticsTO result = analyticsService.getSalaryAnalytics("Rust", "Junior");

        assertThat(result.getByCategory()).isEmpty();
        assertThat(result.getByExperienceLevel()).isEmpty();
    }

    @Test
    void shouldNotMixCurrenciesSalaryUnitsOrEmploymentTypes() {
        OfferEntity plnMonthlyB2b = offer("Java", "Mid", "10000", "12000");
        OfferEntity eurMonthlyB2b = offer("Java", "Mid", "3000", "4000");
        eurMonthlyB2b.setCurrency("EUR");
        OfferEntity plnHourlyB2b = offer("Java", "Mid", "100", "120");
        plnHourlyB2b.setSalaryUnit("hour");
        OfferEntity plnMonthlyEmployment = offer("Java", "Mid", "9000", "11000");
        plnMonthlyEmployment.setEmploymentType("employment");
        when(offerPersistenceService.findForSalaryAnalytics(null, null)).thenReturn(List.of(
                plnMonthlyB2b, eurMonthlyB2b, plnHourlyB2b, plnMonthlyEmployment
        ));

        SalaryAnalyticsTO result = analyticsService.getSalaryAnalytics(null, null);

        assertThat(result.getByCategory()).hasSize(4);
        assertThat(result.getByCategory()).allMatch(statistic -> statistic.getOffersCount() == 1);
    }

    @Test
    void shouldNormalizeSalariesAndBuildContinuousMonthlyTrend() {
        LocalDateTime dateFrom = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime dateTo = LocalDateTime.of(2026, 3, 31, 23, 59);
        List<OfferEntity> offers = List.of(
                datedOffer("HOUR", "100", LocalDateTime.of(2026, 1, 5, 12, 0)),
                datedOffer("DAY", "800", LocalDateTime.of(2026, 1, 10, 12, 0)),
                datedOffer("MONTH", "16800", LocalDateTime.of(2026, 1, 15, 12, 0)),
                datedOffer("YEAR", "201600", LocalDateTime.of(2026, 1, 20, 12, 0))
        );
        when(offerPersistenceService.findForSalaryTrend(
                "Java", "PLN", "b2b", "Senior", dateFrom, dateTo
        )).thenReturn(offers);

        SalaryTrendTO result = analyticsService.getSalaryTrend(
                "Java", "PLN", "b2b", "Senior", dateFrom, dateTo
        );

        assertThat(result.getSalaryUnit()).isEqualTo("MONTH");
        assertThat(result.getPoints()).hasSize(3);
        assertThat(result.getPoints().getFirst().getMonth()).isEqualTo("2026-01");
        assertThat(result.getPoints().getFirst().getAverageSalary()).isEqualByComparingTo("16800.00");
        assertThat(result.getPoints().getFirst().getMedianSalary()).isEqualByComparingTo("16800.00");
        assertThat(result.getPoints().getFirst().getOffersCount()).isEqualTo(4);
        assertThat(result.getPoints().get(1).getMonth()).isEqualTo("2026-02");
        assertThat(result.getPoints().get(1).getAverageSalary()).isNull();
        assertThat(result.getPoints().get(1).getOffersCount()).isZero();
    }

    @Test
    void shouldRejectInvalidSalaryTrendDateRange() {
        LocalDateTime dateFrom = LocalDateTime.of(2026, 2, 1, 0, 0);
        LocalDateTime dateTo = LocalDateTime.of(2026, 1, 1, 0, 0);

        assertThatThrownBy(() -> analyticsService.getSalaryTrend(
                "Java", "PLN", "b2b", null, dateFrom, dateTo
        )).isInstanceOf(ValidationException.class);
    }

    private OfferEntity offer(String technology, String experienceLevel, String salaryMin, String salaryMax) {
        return offer(Set.of(technology), experienceLevel, salaryMin, salaryMax);
    }

    private OfferEntity offer(Set<String> technologies, String experienceLevel, String salaryMin, String salaryMax) {
        OfferEntity offer = new OfferEntity();
        offer.setCategory(technologies.iterator().next());
        offer.setExperienceLevel(experienceLevel);
        offer.setSalaryMin(decimal(salaryMin));
        offer.setSalaryMax(decimal(salaryMax));
        offer.setCurrency("PLN");
        offer.setSalaryUnit("month");
        offer.setEmploymentType("b2b");
        offer.setTechnologies(technologies.stream().map(this::technology).collect(java.util.stream.Collectors.toSet()));
        return offer;
    }

    private TechnologyEntity technology(String name) {
        TechnologyEntity technology = new TechnologyEntity();
        technology.setName(name);
        return technology;
    }

    private OfferEntity datedOffer(String salaryUnit, String salary, LocalDateTime publishedAt) {
        OfferEntity offer = offer("Java", "Senior", salary, salary);
        offer.setSalaryUnit(salaryUnit);
        offer.setPublishedAt(publishedAt);
        return offer;
    }

    private BigDecimal decimal(String value) {
        return value == null ? null : new BigDecimal(value);
    }
}
