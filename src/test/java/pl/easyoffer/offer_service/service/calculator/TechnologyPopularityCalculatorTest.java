package pl.easyoffer.offer_service.service.calculator;

import org.junit.jupiter.api.Test;
import pl.easyoffer.offer_service.model.entity.OfferEntity;
import pl.easyoffer.offer_service.model.entity.TechnologyEntity;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TechnologyPopularityCalculatorTest {

    private static final String SHARED_FINGERPRINT = fingerprint('a');

    private final TechnologyPopularityCalculator calculator = new TechnologyPopularityCalculator();

    @Test
    void shouldCountCrossSourceFingerprintAsOneOfferAndUnionItsTechnologies() {
        OfferEntity justJoinIt = offer("JUST_JOIN_IT", SHARED_FINGERPRINT, "Java", "React");
        OfferEntity theProtocol = offer("THE_PROTOCOL_IT", SHARED_FINGERPRINT, "Java", "Python");

        TechnologyPopularityCalculation result = calculator.calculate(List.of(justJoinIt, theProtocol));

        assertThat(result.rawOfferCount()).isEqualTo(2);
        assertThat(result.uniqueOfferCount()).isEqualTo(1);
        assertThat(result.eligibleOfferCount()).isEqualTo(1);
        assertThat(result.offerCountsByTechnology()).containsExactlyInAnyOrderEntriesOf(
                java.util.Map.of("Java", 1L, "React", 1L, "Python", 1L)
        );
    }

    @Test
    void shouldKeepNullAndSameSourceFingerprintsAsSeparateOffers() {
        TechnologyPopularityCalculation result = calculator.calculate(List.of(
                offer("JUST_JOIN_IT", SHARED_FINGERPRINT, "Java"),
                offer("JUST_JOIN_IT", SHARED_FINGERPRINT, "Java"),
                offer("THE_PROTOCOL_IT", null, "Java"),
                offer("JUST_JOIN_IT", null, "Java")
        ));

        assertThat(result.rawOfferCount()).isEqualTo(4);
        assertThat(result.uniqueOfferCount()).isEqualTo(4);
        assertThat(result.eligibleOfferCount()).isEqualTo(4);
        assertThat(result.offerCountsByTechnology()).containsEntry("Java", 4L);
    }

    @Test
    void shouldExcludeOfferWithoutTechnologyFromRq1Denominator() {
        TechnologyPopularityCalculation result = calculator.calculate(List.of(
                offer("JUST_JOIN_IT", null, "Java"),
                offer("THE_PROTOCOL_IT", null)
        ));

        assertThat(result.uniqueOfferCount()).isEqualTo(2);
        assertThat(result.eligibleOfferCount()).isEqualTo(1);
        assertThat(result.offerCountsByTechnology()).containsOnlyKeys("Java");
    }

    @Test
    void shouldBuildContinuousMonthlyTechnologyCountAndDenominator() {
        TechnologyTrendCalculation result = calculator.calculateMonthlyTrend(
                List.of(
                        datedOffer(2026, 1, 5, "JUST_JOIN_IT", null, "Java"),
                        datedOffer(2026, 1, 10, "THE_PROTOCOL_IT", null, "Python"),
                        datedOffer(2026, 1, 15, "JUST_JOIN_IT", null),
                        datedOffer(2026, 3, 5, "JUST_JOIN_IT", null, "Java")
                ),
                "Java",
                YearMonth.of(2026, 1),
                YearMonth.of(2026, 3)
        );

        assertThat(result.rawOfferCount()).isEqualTo(4);
        assertThat(result.uniqueOfferCount()).isEqualTo(4);
        assertThat(result.eligibleOfferCount()).isEqualTo(3);
        assertThat(result.points()).containsExactly(
                new TechnologyTrendPointCalculation(YearMonth.of(2026, 1), 1, 2),
                new TechnologyTrendPointCalculation(YearMonth.of(2026, 2), 0, 0),
                new TechnologyTrendPointCalculation(YearMonth.of(2026, 3), 1, 1)
        );
    }

    @Test
    void shouldAssignCrossSourceGroupToItsEarliestPublicationMonth() {
        TechnologyTrendCalculation result = calculator.calculateMonthlyTrend(
                List.of(
                        datedOffer(2026, 1, 31, "JUST_JOIN_IT", SHARED_FINGERPRINT, "React"),
                        datedOffer(2026, 2, 1, "THE_PROTOCOL_IT", SHARED_FINGERPRINT, "Java")
                ),
                "Java",
                YearMonth.of(2026, 1),
                YearMonth.of(2026, 2)
        );

        assertThat(result.rawOfferCount()).isEqualTo(2);
        assertThat(result.uniqueOfferCount()).isEqualTo(1);
        assertThat(result.eligibleOfferCount()).isEqualTo(1);
        assertThat(result.points()).containsExactly(
                new TechnologyTrendPointCalculation(YearMonth.of(2026, 1), 1, 1),
                new TechnologyTrendPointCalculation(YearMonth.of(2026, 2), 0, 0)
        );
    }

    private OfferEntity offer(String source, String fingerprint, String... technologies) {
        OfferEntity offer = new OfferEntity();
        offer.setSource(source);
        offer.setDeduplicationFingerprint(fingerprint);
        offer.setTechnologies(java.util.Arrays.stream(technologies)
                .map(this::technology)
                .collect(java.util.stream.Collectors.toSet()));
        return offer;
    }

    private OfferEntity datedOffer(
            int year,
            int month,
            int day,
            String source,
            String fingerprint,
            String... technologies
    ) {
        OfferEntity offer = offer(source, fingerprint, technologies);
        offer.setPublishedAt(LocalDateTime.of(year, month, day, 12, 0));
        return offer;
    }

    private TechnologyEntity technology(String canonicalName) {
        TechnologyEntity technology = new TechnologyEntity();
        technology.setName(canonicalName);
        technology.setCanonicalName(canonicalName);
        technology.setNormalizationVersion("v1");
        return technology;
    }

    private static String fingerprint(char hashCharacter) {
        return "v1:" + String.valueOf(hashCharacter).repeat(64);
    }
}
