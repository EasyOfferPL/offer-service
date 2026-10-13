package pl.easyoffer.offer_service.service.deduplication;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CrossSourceFingerprintV1Test {

    @Test
    void shouldCanonicalizeCaseUnicodeAndWhitespace() {
        String first = CrossSourceFingerprintV1.calculate(
                "  Java\u00A0Developer  ", "ACME", "Warsaw"
        ).orElseThrow();
        String second = CrossSourceFingerprintV1.calculate(
                "java developer", "  acme  ", "WARSAW"
        ).orElseThrow();

        assertThat(first)
                .isEqualTo(second)
                .startsWith("v1:")
                .hasSize(CrossSourceFingerprintV1.FINGERPRINT_LENGTH);
    }

    @Test
    void shouldPreserveMeaningfulFieldDifferences() {
        String warsaw = CrossSourceFingerprintV1.calculate(
                "C++ Developer", "ACME", "Warsaw"
        ).orElseThrow();
        String krakow = CrossSourceFingerprintV1.calculate(
                "C++ Developer", "ACME", "Krakow"
        ).orElseThrow();
        String differentTitle = CrossSourceFingerprintV1.calculate(
                "C Developer", "ACME", "Warsaw"
        ).orElseThrow();

        assertThat(warsaw).isNotEqualTo(krakow).isNotEqualTo(differentTitle);
    }

    @Test
    void shouldSkipFingerprintWhenAnyRequiredFieldIsMissing() {
        assertThat(CrossSourceFingerprintV1.calculate(null, "ACME", "Warsaw")).isEmpty();
        assertThat(CrossSourceFingerprintV1.calculate("Java Developer", " ", "Warsaw")).isEmpty();
        assertThat(CrossSourceFingerprintV1.calculate("Java Developer", "ACME", "")).isEmpty();
    }
}
