package pl.easyoffer.offer_service.service.normalization;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TechnologyNormalizerTest {

    private final TechnologyNormalizer normalizer = new TechnologyNormalizer();

    @Test
    void shouldResolveCaseInsensitiveAliasesAndExposeRuleVersion() {
        TechnologyNormalizationResult result = normalizer.normalize("  apache   KAFKA ")
                .orElseThrow();

        assertThat(result.sourceName()).isEqualTo("apache KAFKA");
        assertThat(result.canonicalName()).isEqualTo("Kafka");
        assertThat(result.version()).isEqualTo("v1");
        assertThat(result.changed()).isTrue();
    }

    @Test
    void shouldPreserveCleanedUnknownValue() {
        TechnologyNormalizationResult result = normalizer.normalize("  Domain\u00A0Specific  Tool ")
                .orElseThrow();

        assertThat(result.sourceName()).isEqualTo("Domain Specific Tool");
        assertThat(result.canonicalName()).isEqualTo("Domain Specific Tool");
        assertThat(result.changed()).isFalse();
    }

    @Test
    void shouldKeepDistinctFrameworkVariantsSeparate() {
        assertThat(normalizer.normalize(".net").orElseThrow().canonicalName()).isEqualTo(".NET");
        assertThat(normalizer.normalize(".NET Core").orElseThrow().canonicalName())
                .isEqualTo(".NET Core");
        assertThat(normalizer.normalize(".NET Framework").orElseThrow().canonicalName())
                .isEqualTo(".NET Framework");
    }

    @Test
    void shouldDiscardBlankValuesAndDeduplicateCanonicalSearchNames() {
        assertThat(normalizer.normalize(" \t ")).isEmpty();
        assertThat(normalizer.normalizeCanonicalNames(List.of(
                "React.js", "react", "unknown", "unknown", " "
        ))).containsExactly("React", "unknown");
    }
}
