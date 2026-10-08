package pl.easyoffer.offer_service.service.calculator;

import org.junit.jupiter.api.Test;
import pl.easyoffer.offer_service.model.entity.OfferEntity;
import pl.easyoffer.offer_service.model.to.SalaryStatisticTO;
import pl.easyoffer.offer_service.model.to.SalaryTrendPointTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class SalaryAnalyticsCalculatorTest {

    private final SalaryAnalyticsCalculator calculator = new SalaryAnalyticsCalculator();

    @Test
    void shouldCalculateCategoryStatisticsAndKeepSalaryDimensionsSeparate() {
        // given
        List<OfferEntity> offers = List.of(
                offer("Java", "Mid", "10000", "14000", "PLN", "month", "b2b"),
                offer("Java", "Mid", "14000", "18000", "PLN", "month", "b2b"),
                offer("Java", "Mid", "18000", "22000", "PLN", "month", "b2b"),
                offer("Java", "Mid", "3000", "4000", "EUR", "month", "b2b"),
                offer("Java", "Mid", "100", "120", "PLN", "hour", "b2b"),
                offer("Java", "Mid", "9000", "11000", "PLN", "month", "employment")
        );

        // when
        List<SalaryStatisticTO> statistics = calculator.calculateByCategory(offers);

        // then
        assertThat(statistics)
                .extracting(
                        SalaryStatisticTO::getGroupValue,
                        SalaryStatisticTO::getCurrency,
                        SalaryStatisticTO::getSalaryUnit,
                        SalaryStatisticTO::getEmploymentType,
                        SalaryStatisticTO::getAverageSalary,
                        SalaryStatisticTO::getMedianSalary,
                        SalaryStatisticTO::getOffersCount
                )
                .containsExactly(
                        tuple("Java", "EUR", "month", "b2b", decimal("3500.00"), decimal("3500.00"), 1L),
                        tuple("Java", "PLN", "hour", "b2b", decimal("110.00"), decimal("110.00"), 1L),
                        tuple("Java", "PLN", "month", "b2b", decimal("16000.00"), decimal("16000.00"), 3L),
                        tuple("Java", "PLN", "month", "employment", decimal("10000.00"), decimal("10000.00"), 1L)
                );
    }

    @Test
    void shouldCalculateExperienceStatisticsUsingAvailableSalaryBoundary() {
        // given
        List<OfferEntity> offers = List.of(
                offer("Backend", "Senior", "10000", null, "PLN", "month", "b2b"),
                offer("Backend", "Senior", null, "14000", "PLN", "month", "b2b")
        );

        // when
        SalaryStatisticTO statistic = calculator.calculateByExperienceLevel(offers).getFirst();

        // then
        assertThat(statistic.getGroupValue()).isEqualTo("Senior");
        assertThat(statistic.getAverageSalary()).isEqualByComparingTo("12000.00");
        assertThat(statistic.getMedianSalary()).isEqualByComparingTo("12000.00");
        assertThat(statistic.getOffersCount()).isEqualTo(2);
    }

    @Test
    void shouldIgnoreOffersWithoutSalaryOrGroupingValue() {
        // given
        List<OfferEntity> offers = List.of(
                offer("Java", "Mid", null, null, "PLN", "month", "b2b"),
                offer(" ", "Mid", "10000", "12000", "PLN", "month", "b2b")
        );

        // when
        List<SalaryStatisticTO> statistics = calculator.calculateByCategory(offers);

        // then
        assertThat(statistics).isEmpty();
    }

    @Test
    void shouldNormalizeSalaryUnitsAndBuildContinuousMonthlyTrend() {
        // given
        LocalDateTime dateFrom = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime dateTo = LocalDateTime.of(2026, 3, 31, 23, 59);
        List<OfferEntity> offers = List.of(
                datedOffer("HOUR", "100", LocalDateTime.of(2026, 1, 5, 12, 0)),
                datedOffer("DAY", "800", LocalDateTime.of(2026, 1, 10, 12, 0)),
                datedOffer("MONTH", "16800", LocalDateTime.of(2026, 1, 15, 12, 0)),
                datedOffer("YEAR", "201600", LocalDateTime.of(2026, 1, 20, 12, 0)),
                datedOffer("MONTH", "12000", LocalDateTime.of(2026, 3, 10, 12, 0))
        );

        // when
        List<SalaryTrendPointTO> trend = calculator.calculateMonthlyTrend(offers, dateFrom, dateTo);

        // then
        assertThat(trend)
                .extracting(
                        SalaryTrendPointTO::getMonth,
                        SalaryTrendPointTO::getAverageSalary,
                        SalaryTrendPointTO::getMedianSalary,
                        SalaryTrendPointTO::getOffersCount
                )
                .containsExactly(
                        tuple("2026-01", decimal("16800.00"), decimal("16800.00"), 4L),
                        tuple("2026-02", null, null, 0L),
                        tuple("2026-03", decimal("12000.00"), decimal("12000.00"), 1L)
                );
    }

    @Test
    void shouldUseCreationDateAsFallbackAndIgnoreOffersWithoutUsableTrendData() {
        // given
        OfferEntity fallbackDateOffer = datedOffer("MONTH", "12000", null);
        fallbackDateOffer.setCreatedAt(LocalDateTime.of(2026, 2, 10, 12, 0));
        OfferEntity missingSalary = datedOffer("MONTH", null, LocalDateTime.of(2026, 2, 11, 12, 0));
        OfferEntity missingDate = datedOffer("MONTH", "15000", null);

        // when
        List<SalaryTrendPointTO> trend = calculator.calculateMonthlyTrend(
                List.of(fallbackDateOffer, missingSalary, missingDate),
                LocalDateTime.of(2026, 2, 1, 0, 0),
                LocalDateTime.of(2026, 2, 28, 23, 59)
        );

        // then
        assertThat(trend).singleElement().satisfies(point -> {
            assertThat(point.getMonth()).isEqualTo("2026-02");
            assertThat(point.getAverageSalary()).isEqualByComparingTo("12000.00");
            assertThat(point.getMedianSalary()).isEqualByComparingTo("12000.00");
            assertThat(point.getOffersCount()).isEqualTo(1);
        });
    }

    private OfferEntity offer(
            String category,
            String experienceLevel,
            String salaryMin,
            String salaryMax,
            String currency,
            String salaryUnit,
            String employmentType
    ) {
        OfferEntity offer = new OfferEntity();
        offer.setCategory(category);
        offer.setExperienceLevel(experienceLevel);
        offer.setSalaryMin(decimal(salaryMin));
        offer.setSalaryMax(decimal(salaryMax));
        offer.setCurrency(currency);
        offer.setSalaryUnit(salaryUnit);
        offer.setEmploymentType(employmentType);
        return offer;
    }

    private OfferEntity datedOffer(String salaryUnit, String salary, LocalDateTime publishedAt) {
        OfferEntity offer = offer("Java", "Senior", salary, salary, "PLN", salaryUnit, "b2b");
        offer.setPublishedAt(publishedAt);
        return offer;
    }

    private BigDecimal decimal(String value) {
        return value == null ? null : new BigDecimal(value);
    }
}
