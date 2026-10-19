package pl.easyoffer.offer_service.performance;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LatencyStatisticsTest {

    @Test
    void calculatesNearestRankPercentilesInMilliseconds() {
        LatencyStatistics.LatencySummary summary = LatencyStatistics.summarize(List.of(
                10_000_000L,
                40_000_000L,
                20_000_000L,
                30_000_000L,
                100_000_000L
        ));

        assertThat(summary.minimumMilliseconds()).isEqualTo(10.0);
        assertThat(summary.p50Milliseconds()).isEqualTo(30.0);
        assertThat(summary.p95Milliseconds()).isEqualTo(100.0);
        assertThat(summary.maximumMilliseconds()).isEqualTo(100.0);
        assertThat(summary.meanMilliseconds()).isEqualTo(40.0);
    }
}
