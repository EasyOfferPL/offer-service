package pl.easyoffer.offer_service.performance;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PerformanceExperimentConfigurationTest {

    @Test
    void parsesStrictlyIncreasingDatasetSizes() {
        assertThat(PerformanceExperimentConfiguration.parseDatasetSizes("1000, 5000,10000"))
                .containsExactly(1000, 5000, 10000);
    }

    @Test
    void rejectsNonIncreasingDatasetSizes() {
        assertThatThrownBy(() -> PerformanceExperimentConfiguration.parseDatasetSizes("1000,1000"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("strictly increasing");
    }

    @Test
    void rejectsWorkloadAboveSafetyBound() {
        assertThatThrownBy(() -> PerformanceExperimentConfiguration.parseDatasetSizes("100001"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("100000");
    }
}
