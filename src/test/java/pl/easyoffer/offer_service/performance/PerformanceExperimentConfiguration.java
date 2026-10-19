package pl.easyoffer.offer_service.performance;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

record PerformanceExperimentConfiguration(
        List<Integer> datasetSizes,
        int warmupRequests,
        int measuredRequests,
        int technologiesPerOffer,
        LocalDate dateFrom,
        LocalDate dateTo,
        Path reportDirectory,
        String codeRevision,
        String workingTreeState
) {

    static final String DEFAULT_DATASET_SIZES = "1000,5000,10000";
    static final int MAX_DATASET_SIZE = 100_000;
    static final int MAX_REQUEST_REPETITIONS = 50;
    static final int AVAILABLE_TECHNOLOGY_COUNT = 12;

    static PerformanceExperimentConfiguration fromSystemProperties() {
        int currentYear = LocalDate.now().getYear();
        List<Integer> datasetSizes = parseDatasetSizes(
                System.getProperty("performance.datasetSizes", DEFAULT_DATASET_SIZES)
        );
        int warmupRequests = positiveOrZeroProperty("performance.warmupRequests", 2);
        int measuredRequests = positiveProperty("performance.measuredRequests", 10);
        int technologiesPerOffer = positiveProperty("performance.technologiesPerOffer", 3);
        LocalDate dateFrom = LocalDate.parse(System.getProperty(
                "performance.dateFrom", currentYear + "-01-01"
        ));
        LocalDate dateTo = LocalDate.parse(System.getProperty(
                "performance.dateTo", currentYear + "-12-31"
        ));
        Path reportDirectory = Path.of(System.getProperty(
                "performance.reportDirectory", "target/performance"
        ));
        String codeRevision = requiredProperty("performance.codeRevision");
        String workingTreeState = requiredProperty("performance.workingTreeState");

        if (warmupRequests > MAX_REQUEST_REPETITIONS
                || measuredRequests > MAX_REQUEST_REPETITIONS) {
            throw new IllegalArgumentException(
                    "Request repetitions must not exceed " + MAX_REQUEST_REPETITIONS
            );
        }
        if (technologiesPerOffer > AVAILABLE_TECHNOLOGY_COUNT) {
            throw new IllegalArgumentException(
                    "technologiesPerOffer must not exceed " + AVAILABLE_TECHNOLOGY_COUNT
            );
        }
        if (dateTo.isBefore(dateFrom)) {
            throw new IllegalArgumentException("performance.dateTo must not precede performance.dateFrom");
        }
        LocalDate currentDate = LocalDate.now();
        if (currentDate.isBefore(dateFrom) || currentDate.isAfter(dateTo)) {
            throw new IllegalArgumentException(
                    "The experiment date range must include the current date for createdAt measurements"
            );
        }
        if (!workingTreeState.equals("clean") && !workingTreeState.equals("dirty")) {
            throw new IllegalArgumentException("performance.workingTreeState must be clean or dirty");
        }

        return new PerformanceExperimentConfiguration(
                datasetSizes,
                warmupRequests,
                measuredRequests,
                technologiesPerOffer,
                dateFrom,
                dateTo,
                reportDirectory,
                codeRevision,
                workingTreeState
        );
    }

    static List<Integer> parseDatasetSizes(String rawDatasetSizes) {
        List<Integer> sizes;
        try {
            sizes = Arrays.stream(rawDatasetSizes.split(","))
                    .map(String::trim)
                    .map(Integer::valueOf)
                    .toList();
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "performance.datasetSizes must be a comma-separated integer list", exception
            );
        }
        if (sizes.isEmpty() || sizes.stream().anyMatch(size -> size < 1)) {
            throw new IllegalArgumentException("performance.datasetSizes must contain positive values");
        }
        for (int index = 1; index < sizes.size(); index++) {
            if (sizes.get(index) <= sizes.get(index - 1)) {
                throw new IllegalArgumentException(
                        "performance.datasetSizes must be strictly increasing"
                );
            }
        }
        if (sizes.getLast() > MAX_DATASET_SIZE) {
            throw new IllegalArgumentException(
                    "Largest dataset size must not exceed " + MAX_DATASET_SIZE
            );
        }
        return sizes;
    }

    private static int positiveProperty(String name, int defaultValue) {
        int value = Integer.parseInt(System.getProperty(name, Integer.toString(defaultValue)));
        if (value < 1) {
            throw new IllegalArgumentException(name + " must be positive");
        }
        return value;
    }

    private static int positiveOrZeroProperty(String name, int defaultValue) {
        int value = Integer.parseInt(System.getProperty(name, Integer.toString(defaultValue)));
        if (value < 0) {
            throw new IllegalArgumentException(name + " must not be negative");
        }
        return value;
    }

    private static String requiredProperty(String name) {
        String value = System.getProperty(name);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " is required");
        }
        return value.trim();
    }
}
