package pl.easyoffer.offer_service.performance;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import pl.easyoffer.offer_service.repository.OfferRepository;
import pl.easyoffer.offer_service.repository.SynchronizationRunRepository;
import pl.easyoffer.offer_service.repository.TechnologyRepository;
import pl.easyoffer.offer_service.service.OfferService;
import pl.easyoffer.offer_service.service.persistence.OfferPersistenceOutcome;

import javax.sql.DataSource;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("performance")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BoundedPerformanceExperimentIT {

    private static final String EXPERIMENT_ID = "F-016";
    private static final int REPORT_SCHEMA_VERSION = 1;
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(60);
    private static final double NANOS_PER_SECOND = 1_000_000_000.0;
    private static final String DISCLAIMER = "Synthetic bounded experiment; results describe only the recorded "
            + "environment and workload and must not be generalized to production traffic.";

    @LocalServerPort
    private int port;

    @Autowired
    private Environment environment;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private OfferService offerService;

    @Autowired
    private OfferRepository offerRepository;

    @Autowired
    private TechnologyRepository technologyRepository;

    @Autowired
    private SynchronizationRunRepository synchronizationRunRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private ObjectMapper objectMapper;

    private PerformanceExperimentConfiguration configuration;
    private HttpClient httpClient;

    @BeforeAll
    void prepareDedicatedDatabase() throws SQLException {
        assertThat(Boolean.getBoolean("performance.enabled"))
                .as("Run only through the explicit F-016 performance command")
                .isTrue();
        String jdbcUrl = environment.getRequiredProperty("spring.datasource.url");
        DedicatedPerformanceDatabaseGuard.verify(
                jdbcUrl,
                System.getProperty("performance.confirmDatabase")
        );
        assertThat(offerRepository.count()).as("offers table must be empty").isZero();
        assertThat(technologyRepository.count()).as("technologies table must be empty").isZero();
        assertThat(synchronizationRunRepository.count())
                .as("synchronization_runs table must be empty")
                .isZero();
        configuration = PerformanceExperimentConfiguration.fromSystemProperties();
        httpClient = HttpClient.newBuilder()
                .connectTimeout(REQUEST_TIMEOUT)
                .build();
    }

    @AfterAll
    void cleanDedicatedDatabase() {
        if (configuration != null) {
            clearSyntheticData();
        }
    }

    @Test
    void runBoundedExperiment() throws Exception {
        SyntheticOfferFactory offerFactory = new SyntheticOfferFactory(
                configuration.dateFrom(),
                configuration.dateTo(),
                configuration.technologiesPerOffer()
        );
        List<PerformanceExperimentReport.ScaleMeasurement> scaleMeasurements = new ArrayList<>();
        int currentOfferCount = 0;
        long cumulativeImportDurationNanos = 0;

        for (int targetOfferCount : configuration.datasetSizes()) {
            ImportResult importResult = importOffers(
                    offerFactory,
                    currentOfferCount,
                    targetOfferCount
            );
            cumulativeImportDurationNanos += importResult.durationNanos();
            currentOfferCount = targetOfferCount;
            assertThat(offerRepository.count()).isEqualTo(targetOfferCount);

            List<PerformanceExperimentReport.EndpointMeasurement> endpointMeasurements =
                    benchmarkEndpoints();
            scaleMeasurements.add(new PerformanceExperimentReport.ScaleMeasurement(
                    targetOfferCount,
                    new PerformanceExperimentReport.ImportMeasurement(
                            importResult.addedOfferCount(),
                            importResult.durationNanos() / 1_000_000.0,
                            importResult.addedOfferCount()
                                    / (importResult.durationNanos() / NANOS_PER_SECOND),
                            cumulativeImportDurationNanos / 1_000_000.0,
                            targetOfferCount / (cumulativeImportDurationNanos / NANOS_PER_SECOND)
                    ),
                    endpointMeasurements
            ));
        }

        PerformanceExperimentReport report = new PerformanceExperimentReport(
                EXPERIMENT_ID,
                REPORT_SCHEMA_VERSION,
                Instant.now(),
                configuration.codeRevision(),
                configuration.workingTreeState(),
                DISCLAIMER,
                runtimeEnvironment(),
                databaseEnvironment(),
                new PerformanceExperimentReport.Workload(
                        configuration.datasetSizes(),
                        configuration.warmupRequests(),
                        configuration.measuredRequests(),
                        configuration.technologiesPerOffer(),
                        configuration.dateFrom(),
                        configuration.dateTo(),
                        "OfferService.createOrUpdateForSynchronization",
                        "sequential HTTP requests against an embedded random-port server"
                ),
                scaleMeasurements
        );
        Path reportPath = writeReport(report);
        System.out.println("F-016 report: " + reportPath.toAbsolutePath());

        assertThat(reportPath).isRegularFile();
        assertThat(report.measurements()).hasSize(configuration.datasetSizes().size());
    }

    private ImportResult importOffers(
            SyntheticOfferFactory offerFactory,
            int firstOfferIndex,
            int targetOfferCount
    ) {
        long startedAt = System.nanoTime();
        for (int offerIndex = firstOfferIndex; offerIndex < targetOfferCount; offerIndex++) {
            OfferPersistenceOutcome outcome = offerService.createOrUpdateForSynchronization(
                    offerFactory.create(offerIndex)
            );
            if (outcome != OfferPersistenceOutcome.CREATED) {
                throw new IllegalStateException(
                        "Synthetic offer was not created at index " + offerIndex
                );
            }
        }
        long durationNanos = System.nanoTime() - startedAt;
        int addedOfferCount = targetOfferCount - firstOfferIndex;
        return new ImportResult(addedOfferCount, durationNanos);
    }

    private List<PerformanceExperimentReport.EndpointMeasurement> benchmarkEndpoints()
            throws IOException, InterruptedException {
        String dateRange = "dateFrom=" + configuration.dateFrom()
                + "&dateTo=" + configuration.dateTo();
        List<EndpointWorkload> workloads = List.of(
                new EndpointWorkload(
                        "offers-page",
                        "/offers?page=0&size=50&sort=id,asc"
                ),
                new EndpointWorkload(
                        "technology-popularity",
                        "/v1.0/analytics/technologies/popularity?" + dateRange + "&page=0&size=50"
                ),
                new EndpointWorkload(
                        "technology-trend",
                        "/v1.0/analytics/technologies/trend?technologyName=Java&" + dateRange
                ),
                new EndpointWorkload(
                        "salary-distribution",
                        "/v1.0/analytics/salaries"
                ),
                new EndpointWorkload(
                        "data-completeness",
                        "/v1.0/analytics/data-completeness?" + dateRange
                )
        );
        List<PerformanceExperimentReport.EndpointMeasurement> measurements = new ArrayList<>();
        for (EndpointWorkload workload : workloads) {
            measurements.add(measureEndpoint(workload));
        }
        return measurements;
    }

    private PerformanceExperimentReport.EndpointMeasurement measureEndpoint(
            EndpointWorkload workload
    ) throws IOException, InterruptedException {
        for (int warmup = 0; warmup < configuration.warmupRequests(); warmup++) {
            executeRequest(workload.path());
        }

        List<Long> durations = new ArrayList<>();
        int minimumResponseBytes = Integer.MAX_VALUE;
        int maximumResponseBytes = 0;
        for (int repetition = 0; repetition < configuration.measuredRequests(); repetition++) {
            MeasuredResponse response = executeRequest(workload.path());
            durations.add(response.durationNanos());
            minimumResponseBytes = Math.min(minimumResponseBytes, response.responseBytes());
            maximumResponseBytes = Math.max(maximumResponseBytes, response.responseBytes());
        }

        return new PerformanceExperimentReport.EndpointMeasurement(
                workload.name(),
                workload.path(),
                durations.size(),
                durations.size() / (durations.stream().mapToLong(Long::longValue).sum()
                        / NANOS_PER_SECOND),
                minimumResponseBytes,
                maximumResponseBytes,
                LatencyStatistics.summarize(durations)
        );
    }

    private MeasuredResponse executeRequest(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + port + path))
                .timeout(REQUEST_TIMEOUT)
                .GET()
                .build();
        long startedAt = System.nanoTime();
        HttpResponse<byte[]> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofByteArray()
        );
        long durationNanos = System.nanoTime() - startedAt;
        assertThat(response.statusCode()).as(path).isEqualTo(200);
        return new MeasuredResponse(durationNanos, response.body().length);
    }

    private PerformanceExperimentReport.RuntimeEnvironment runtimeEnvironment() {
        Runtime runtime = Runtime.getRuntime();
        return new PerformanceExperimentReport.RuntimeEnvironment(
                System.getProperty("os.name") + " " + System.getProperty("os.version"),
                System.getProperty("os.arch"),
                runtime.availableProcessors(),
                runtime.maxMemory(),
                System.getProperty("java.version"),
                System.getProperty("java.vendor")
        );
    }

    private PerformanceExperimentReport.DatabaseEnvironment databaseEnvironment()
            throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metadata = connection.getMetaData();
            return new PerformanceExperimentReport.DatabaseEnvironment(
                    metadata.getDatabaseProductName(),
                    metadata.getDatabaseProductVersion(),
                    metadata.getDriverName() + " " + metadata.getDriverVersion(),
                    connection.getCatalog()
            );
        }
    }

    private Path writeReport(PerformanceExperimentReport report) throws IOException {
        Files.createDirectories(configuration.reportDirectory());
        String timestamp = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")
                .withZone(java.time.ZoneOffset.UTC)
                .format(report.generatedAt());
        Path versionedReport = configuration.reportDirectory()
                .resolve("f016-results-" + timestamp + ".json");
        Path latestReport = configuration.reportDirectory().resolve("f016-results-latest.json");
        byte[] serializedReport = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsBytes(report);
        Files.write(
                versionedReport,
                serializedReport,
                StandardOpenOption.CREATE_NEW,
                StandardOpenOption.WRITE
        );
        Files.write(
                latestReport,
                serializedReport,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE
        );
        return versionedReport;
    }

    private void clearSyntheticData() {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            entityManager.createNativeQuery("DELETE FROM job_offer_technologies").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM offers").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM technologies").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM synchronization_runs").executeUpdate();
        });
    }

    private record EndpointWorkload(String name, String path) {
    }

    private record MeasuredResponse(long durationNanos, int responseBytes) {
    }

    private record ImportResult(int addedOfferCount, long durationNanos) {
    }
}
