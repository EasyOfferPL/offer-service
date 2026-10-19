package pl.easyoffer.offer_service.performance;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

record PerformanceExperimentReport(
        String experimentId,
        int reportSchemaVersion,
        Instant generatedAt,
        String codeRevision,
        String workingTreeState,
        String disclaimer,
        RuntimeEnvironment runtime,
        DatabaseEnvironment database,
        Workload workload,
        List<ScaleMeasurement> measurements
) {

    record RuntimeEnvironment(
            String operatingSystem,
            String architecture,
            int availableProcessors,
            long maximumHeapBytes,
            String javaVersion,
            String javaVendor
    ) {
    }

    record DatabaseEnvironment(
            String product,
            String version,
            String driver,
            String databaseName
    ) {
    }

    record Workload(
            List<Integer> datasetSizes,
            int warmupRequests,
            int measuredRequests,
            int technologiesPerOffer,
            LocalDate dateFrom,
            LocalDate dateTo,
            String importPath,
            String requestExecution
    ) {
    }

    record ScaleMeasurement(
            int totalOfferCount,
            ImportMeasurement importMeasurement,
            List<EndpointMeasurement> endpointMeasurements
    ) {
    }

    record ImportMeasurement(
            int addedOfferCount,
            double incrementalDurationMilliseconds,
            double incrementalOffersPerSecond,
            double cumulativeDurationMilliseconds,
            double cumulativeOffersPerSecond
    ) {
    }

    record EndpointMeasurement(
            String name,
            String path,
            int successfulRequests,
            double sequentialRequestsPerSecond,
            int minimumResponseBytes,
            int maximumResponseBytes,
            LatencyStatistics.LatencySummary latency
    ) {
    }
}
