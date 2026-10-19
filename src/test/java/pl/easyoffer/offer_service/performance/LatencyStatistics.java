package pl.easyoffer.offer_service.performance;

import java.util.List;

final class LatencyStatistics {

    private static final double NANOS_PER_MILLISECOND = 1_000_000.0;

    private LatencyStatistics() {
    }

    static LatencySummary summarize(List<Long> durationNanos) {
        if (durationNanos.isEmpty()) {
            throw new IllegalArgumentException("At least one latency sample is required");
        }
        List<Long> sortedDurations = durationNanos.stream().sorted().toList();
        double meanNanos = durationNanos.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElseThrow();
        return new LatencySummary(
                toMilliseconds(sortedDurations.getFirst()),
                toMilliseconds(nearestRank(sortedDurations, 0.50)),
                toMilliseconds(nearestRank(sortedDurations, 0.95)),
                toMilliseconds(sortedDurations.getLast()),
                meanNanos / NANOS_PER_MILLISECOND
        );
    }

    private static long nearestRank(List<Long> sortedDurations, double percentile) {
        int oneBasedRank = (int) Math.ceil(percentile * sortedDurations.size());
        return sortedDurations.get(Math.max(oneBasedRank - 1, 0));
    }

    private static double toMilliseconds(long durationNanos) {
        return durationNanos / NANOS_PER_MILLISECOND;
    }

    record LatencySummary(
            double minimumMilliseconds,
            double p50Milliseconds,
            double p95Milliseconds,
            double maximumMilliseconds,
            double meanMilliseconds
    ) {
    }
}
